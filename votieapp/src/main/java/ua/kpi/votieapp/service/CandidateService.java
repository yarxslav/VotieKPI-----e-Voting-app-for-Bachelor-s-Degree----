package ua.kpi.votieapp.service;

import ua.kpi.votieapp.dto.ImageDto;
import ua.kpi.votieapp.entity.Candidate;

import java.util.List;
import java.util.Optional;

public interface CandidateService {
    void create(Candidate candidate);

    Optional<Candidate> get(Long candidateId);

    List<Candidate> getAll();

    Candidate update(Candidate candidate);

    void delete(Candidate candidate);

    void saveImage(byte[] image, Candidate candidate);

    byte[] getImage(Long candidateId);

    List<ImageDto> getAllImages();

    boolean deleteImage(Long candidateId);
}
