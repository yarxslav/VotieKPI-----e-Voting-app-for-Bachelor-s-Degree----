package ua.kpi.votieapp.dao;

import ua.kpi.votieapp.entity.VoteResult;

import java.util.List;
import java.util.Optional;

public interface VoteResultDao {

    void create(VoteResult voteResult);

    Optional<VoteResult> get(Long voteResultId);

    List<VoteResult> getAll();

    VoteResult update(VoteResult voteResult);

    void delete(VoteResult voteResult);

    boolean hasUserVoted(Long userId, Long votingId);

    List<VoteResult> getByVotingId(Long votingId);
}
