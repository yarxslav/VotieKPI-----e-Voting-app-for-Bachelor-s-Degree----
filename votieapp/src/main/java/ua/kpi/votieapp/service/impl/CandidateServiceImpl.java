package ua.kpi.votieapp.service.impl;

import ua.kpi.votieapp.config.AppConfig;
import ua.kpi.votieapp.dao.CandidateDao;
import ua.kpi.votieapp.dto.ImageDto;
import ua.kpi.votieapp.entity.Candidate;
import ua.kpi.votieapp.exception.InvalidFileTypeException;
import ua.kpi.votieapp.service.CandidateService;
import ua.kpi.votieapp.util.FileUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CandidateServiceImpl implements CandidateService {
    private static final Logger logger = LoggerFactory.getLogger(CandidateServiceImpl.class);

    @Autowired
    private CandidateDao candidateDao;

    @Autowired
    private AppConfig appConfig;

    @Override
    public void create(Candidate candidate) {
        try {
            candidateDao.create(candidate);
        } catch (Exception e) {
            logger.error("Error creating candidate: {}", e.getMessage());
        }
    }

    @Override
    public Optional<Candidate> get(Long candidateId) {
        return candidateDao.get(candidateId);
    }

    @Override
    public List<Candidate> getAll() {
        return candidateDao.getAll();
    }

    @Override
    public Candidate update(Candidate candidate) {
        return candidateDao.update(candidate);
    }

    @Override
    public void delete(Candidate candidate) {
        try {
            deleteImage(candidate.getId());
            candidateDao.delete(candidate);
        } catch (Exception e) {
            logger.error("Error deleting candidate with ID {}: {}", candidate.getId(), e.getMessage());
        }
    }

    @Override
    public void saveImage(byte[] image, Candidate candidate) {
        String fileExtension = getFileExtension(image);

        String uploadDir = appConfig.getImagesPath();
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists() && !uploadDirFile.mkdirs()) {
            logger.error("Unable to create directory: {}", uploadDir);
            throw new RuntimeException("Unable to create directory");
        }

        String fileName = "%s.%s".formatted(UUID.randomUUID(), fileExtension);
        File file = new File("%s%s".formatted(uploadDir, fileName));

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(image);
        } catch (IOException e) {
            logger.error("Failed to save file for candidate ID {}: {}", candidate.getId(), e.getMessage());
            throw new RuntimeException("Failed to save file");
        }

        candidate.setImageName(fileName);
        try {
            update(candidate);
        } catch (Exception e) {
            logger.error("Error updating candidate after saving image for candidate ID {}: {}", candidate.getId(), e.getMessage());
        }
    }

    @Override
    public byte[] getImage(Long candidateId) {
        String imageName = candidateDao.getImageName(candidateId);
        if (imageName == null) {
            return null;
        }

        try {
            return Files.readAllBytes(Paths.get("%s%s".formatted(appConfig.getImagesPath(), imageName)));
        } catch (IOException e) {
            logger.error("Error reading image file for candidate ID {}: {}", candidateId, e.getMessage());
            return null;
        }
    }

    @Override
    public List<ImageDto> getAllImages() {
        List<Candidate> candidates = candidateDao.getAll();
        List<ImageDto> images = new ArrayList<>();

        for (Candidate candidate : candidates) {
            String imageName = candidate.getImageName();
            if (imageName != null) {
                try {
                    byte[] image = Files.readAllBytes(Paths.get("%s%s".formatted(appConfig.getImagesPath(), imageName)));
                    String base64Image = Base64.getEncoder().encodeToString(image);
                    images.add(new ImageDto(candidate.getId(), base64Image));
                } catch (IOException e) {
                    logger.error("Error reading candidate image files.");
                }
            }
        }
        return images;
    }

    @Override
    public boolean deleteImage(Long candidateId) {
        String imageName = candidateDao.getImageName(candidateId);
        if (imageName == null) {
            return false;
        }

        try {
            Files.delete(Paths.get(appConfig.getImagesPath(), imageName));
        } catch (IOException e) {
            logger.error("Error deleting image file for candidate ID {}: {}", candidateId, e.getMessage());
            return false;
        }

        Optional<Candidate> optionalCandidate = candidateDao.get(candidateId);
        if (optionalCandidate.isPresent()) {
            Candidate candidate = optionalCandidate.get();
            candidate.setImageName(null);
            try {
                candidateDao.update(candidate);
                return true;
            } catch (Exception e) {
                logger.error("Error updating candidate after deleting image for candidate ID {}: {}", candidateId, e.getMessage());
                return false;
            }
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
}
