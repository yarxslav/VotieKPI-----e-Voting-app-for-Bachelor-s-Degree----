package ua.kpi.votieapp.service;

import ua.kpi.votieapp.dto.ShowVotingResultsDto;
import ua.kpi.votieapp.dto.VoteResultDto;
import ua.kpi.votieapp.entity.VoteResult;

import java.util.List;
import java.util.Optional;

public interface VoteResultService {

    void create(VoteResultDto voteResultDto);

    Optional<VoteResult> get(Long votingId);

    List<VoteResult> getAll();

    void update(Long voteResultId);

    void delete(VoteResult voteResult);

    boolean hasUserVoted(Long userId, Long votingId);

    List<VoteResult> getByVotingId(Long votingId);

    ShowVotingResultsDto getShowVotingResults(Long votingId);
}
