package ua.kpi.votieapp.dao;

import ua.kpi.votieapp.entity.Candidate;

import java.util.List;
import java.util.Optional;

public interface CandidateDao {

    void create(Candidate candidate);

    Optional<Candidate> get(Long candidateId);

    List<Candidate> getAll();

    List<Candidate> getAllForVote(Long voteId);

    Candidate update(Candidate candidate);

    void delete(Candidate candidate);

    String getImageName(Long candidateId);
}
