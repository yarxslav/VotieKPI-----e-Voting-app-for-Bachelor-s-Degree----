package ua.kpi.votieapp.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import ua.kpi.votieapp.entity.Voting;

public interface VotingService {
    Voting create(String votingString, MultipartFile[] imageFile);

    Optional<Voting> get(Long votingId);

    List<Voting> getAll();

    Voting update(Long votingId, Map<String, Object> payload);

    void delete(Voting voting);

    List<Voting> searchByName(String name);

    List<Voting> searchByPublicId(String publicId);

    List<String> getPublicIds(Long votingId);
}
