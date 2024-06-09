package ua.kpi.votieapp.dao;

import ua.kpi.votieapp.entity.Voting;

import java.util.List;
import java.util.Optional;

public interface VotingDao {

    void create(Voting voting);

    Optional<Voting> get(Long votingId);

    List<Voting> getAll();

    Voting update(Voting voting);

    void delete(Voting voting);

    List<Voting> searchByName(String name);

    List<Voting> searchByPublicId(String publicId);

    List<String> getPublicIds(Long votingId);
}
