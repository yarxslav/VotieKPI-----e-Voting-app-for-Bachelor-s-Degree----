package ua.kpi.votieapp.service.impl;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import ua.kpi.votieapp.config.AppConfig;
import ua.kpi.votieapp.dao.CandidateDao;
import ua.kpi.votieapp.dao.VotingDao;
import ua.kpi.votieapp.entity.Candidate;
import ua.kpi.votieapp.entity.Voting;
import ua.kpi.votieapp.exception.InvalidFileTypeException;
import ua.kpi.votieapp.model.VotingStatus;
import ua.kpi.votieapp.service.VotingService;
import ua.kpi.votieapp.util.FileUtil;

@Service
public class VotingServiceImpl implements VotingService {
    private static final Logger logger = LoggerFactory.getLogger(VotingServiceImpl.class);

    @Autowired
    private VotingDao votingDao;

    @Autowired
    private CandidateDao candidateDao;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private Gson gson;

    @Override
    public Voting create(String votingString, MultipartFile[] imageFiles) {
        Voting voting;
        try {
            voting = gson.fromJson(votingString, Voting.class);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing voting JSON", e);
        }

        if (imageFiles.length != voting.getCandidates().size()) {
            throw new IllegalArgumentException("Number of images does not match the number of candidates");
        }

        for (int i = 0; i < imageFiles.length; i++) {
            MultipartFile imageFile = imageFiles[i];
            String fileName = saveImage(imageFile, voting);
            voting.getCandidates().get(i).setImageName(fileName);
        }

        voting.setStatus(getVotingStatus(voting.getDateTimeFrom(), voting.getDateTimeTo()));
        votingDao.create(voting);
        return voting;
    }

    @Override
    public Optional<Voting> get(Long votingId) {
        Optional<Voting> optionalVoting = votingDao.get(votingId);
        if (optionalVoting.isPresent() && optionalVoting.get().getStatus() != VotingStatus.SUSPENDED) {
            optionalVoting.ifPresent(this::validateAndUpdateVotingStatus);
        }
        return optionalVoting;
    }

    @Override
    public List<Voting> getAll() {
        List<Voting> votings = votingDao.getAll();
        votings.stream()
                .filter(voting -> voting.getStatus() != VotingStatus.SUSPENDED)
                .forEach(this::validateAndUpdateVotingStatus);
        return votings;
    }

    @Override
    public Voting update(Long votingId, Map<String, Object> payload) {
        Voting voting = getVoting(votingId, payload);

        List<Map<String, String>> candidateImagesList = (List<Map<String, String>>) payload.get("candidateImages");
        Map<Long, byte[]> candidateImages = new HashMap<>();
        for (Map<String, String> map : candidateImagesList) {
            Long candidateId = Long.valueOf(String.valueOf(map.get("candidateId")));
            String imageString = map.get("image");
            byte[] imageBytes = Base64.getDecoder().decode(imageString);
            candidateImages.put(candidateId, imageBytes);
        }

        updateImages(votingId, candidateImages, voting.getCandidates());
        try {
            return votingDao.update(voting);
        } catch (Exception e) {
            logger.error("Error updating voting: {}", e.getMessage());
            throw new RuntimeException("Failed to update voting", e);
        }
    }

    @Override
    public void delete(Voting voting) {
        votingDao.delete(voting);
        try {
            List<Candidate> candidates = voting.getCandidates();
            for (Candidate candidate : candidates) {
                deleteImage(candidate.getImageName());
            }
        } catch (Exception e) {
            logger.error("Error deleting voting with ID {}: {}", voting.getId(), e.getMessage());
            throw new RuntimeException("Failed to delete voting", e);
        }
    }

    @Override
    public List<Voting> searchByName(String name) {
        return votingDao.searchByName(name);
    }

    @Override
    public List<Voting> searchByPublicId(String publicId) {
        return votingDao.searchByPublicId(publicId);
    }

    @Override
    public List<String> getPublicIds(Long voteResultId) {
        return votingDao.getPublicIds(voteResultId);
    }

    private String saveImage(MultipartFile image, Voting voting) {
        byte[] imageBytes = null;
        try {
            imageBytes = image.getBytes();
        } catch (IOException e) {
            logger.error("Failed to read bytes from image file for voting ID {}: {}", voting.getId(), e.getMessage());
        }

        return saveImageBytes(voting.getId(), imageBytes);
    }

    private String saveImageBytes(Long votingId, byte[] imageBytes) {
        String fileExtension = getFileExtension(imageBytes);

        String uploadDir = appConfig.getImagesPath();
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists() && !uploadDirFile.mkdirs()) {
            logger.error("Unable to create directory: {}", uploadDir);
        }

        String fileName = String.format("%s.%s", UUID.randomUUID(), fileExtension);
        File file = new File(uploadDir, fileName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(imageBytes);
        } catch (IOException e) {
            logger.error("Failed to save file for voting ID {}: {}", votingId, e.getMessage());
        }
        return fileName;
    }

    private void updateImages(Long votingId, Map<Long, byte[]> candidateImages, List<Candidate> updatedCandidates) {
        List<Candidate> oldCandidates = candidateDao.getAllForVote(votingId);

        for (Candidate oldCandidate : oldCandidates) {
            byte[] newImageBytes = candidateImages.get(oldCandidate.getId());
            if (newImageBytes != null) {
                String newFileName = saveImageBytes(votingId, newImageBytes);

                int indexOfUpdated = -1;
                for (int i = 0; i < updatedCandidates.size(); i++) {
                    if (updatedCandidates.get(i).getId().equals(oldCandidate.getId())) {
                        indexOfUpdated = i;
                        break;
                    }
                }

                if (indexOfUpdated != -1) {
                    String oldFileName = updatedCandidates.get(indexOfUpdated).getImageName();
                    updatedCandidates.get(indexOfUpdated).setImageName(newFileName);
                    if (oldFileName != null) {
                        deleteImage(oldFileName);
                        logger.info("Deleted old image for candidate ID {}", oldCandidate.getId());
                    }
                }
                logger.info("Updated image for candidate ID {}", oldCandidate.getId());
            }
        }

        for (Candidate oldCandidate : oldCandidates) {
            boolean isInUpdated = updatedCandidates.stream().anyMatch(c -> c.getId().equals(oldCandidate.getId()));
            if (!isInUpdated) {
                String oldFileName = oldCandidate.getImageName();
                if (oldFileName != null) {
                    deleteImage(oldFileName);
                    logger.info("Deleted image for candidate ID {}", oldCandidate.getId());
                }
            }
        }

        for (Candidate newCandidate : updatedCandidates) {
            if (newCandidate.getId() == null) {
                byte[] imageBytes = candidateImages.get(newCandidate.getId());
                if (imageBytes != null) {
                    String fileName = saveImageBytes(votingId, imageBytes);
                    newCandidate.setImageName(fileName);
                    logger.info("Saved new image for candidate ID {}", newCandidate.getId());
                }
            }
        }
    }

    private boolean deleteImage(String imageName) {
        if (imageName == null) {
            return false;
        }

        try {
            Files.delete(Paths.get(appConfig.getImagesPath(), imageName));
        } catch (IOException e) {
            logger.error("Error deleting image file: {}", e.getMessage());
            return false;
        }

        return false;
    }

    private String getFileExtension(byte[] image) {
        return switch (FileUtil.getFileType(image)) {
            case "jpg" -> "jpg";
            case "jpeg" -> "jpeg";
            case "png" -> "png";
            default -> throw new InvalidFileTypeException("Invalid file type");
        };
    }

    private VotingStatus getVotingStatus(LocalDateTime dateFrom, LocalDateTime dateTo) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(dateFrom) && now.isBefore(dateTo)) {
            return VotingStatus.ACTIVE;
        } else if (now.isBefore(dateFrom)) {
            return VotingStatus.SCHEDULED;
        } else {
            return VotingStatus.COMPLETED;
        }
    }

    private Voting getVoting(Long votingId, Map<String, Object> payload) {
        String votingJson = gson.toJson(payload.get("voting"));
        Voting voting = gson.fromJson(votingJson, Voting.class);
        voting.setId(votingId);
        return voting;
    }

    private void validateAndUpdateVotingStatus(Voting voting) {
        VotingStatus currentStatus = voting.getStatus();
        VotingStatus newStatus = getVotingStatus(voting.getDateTimeFrom(), voting.getDateTimeTo());

        if (!currentStatus.equals(newStatus)) {
            voting.setStatus(newStatus);
            votingDao.update(voting);
        }
    }
}