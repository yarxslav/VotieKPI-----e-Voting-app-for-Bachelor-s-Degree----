package ua.kpi.votieapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.kpi.votieapp.dao.CandidateDao;
import ua.kpi.votieapp.dao.UserDao;
import ua.kpi.votieapp.dao.VoteResultDao;
import ua.kpi.votieapp.dao.VotingDao;
import ua.kpi.votieapp.dto.CandidatePercentageDto;
import ua.kpi.votieapp.dto.ShowVotingResultsDto;
import ua.kpi.votieapp.dto.VoteResultDto;
import ua.kpi.votieapp.entity.Candidate;
import ua.kpi.votieapp.entity.User;
import ua.kpi.votieapp.entity.VoteResult;
import ua.kpi.votieapp.entity.Voting;
import ua.kpi.votieapp.exception.ResourceNotFoundException;
import ua.kpi.votieapp.service.VoteResultService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VoteResultServiceImpl implements VoteResultService {

    @Autowired
    private VoteResultDao voteResultDao;

    @Autowired
    private VotingDao votingDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CandidateDao candidateDao;

    @Override
    public void create(VoteResultDto voteResultDto) {
        Voting voting = votingDao.get(voteResultDto.getVotingId())
                .orElseThrow(() -> new ResourceNotFoundException("Voting not found"));
        User user = userDao.get(voteResultDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Candidate candidate = candidateDao.get(voteResultDto.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        voteResultDao.create(new VoteResult(voting, user, candidate));
    }

    @Override
    public List<VoteResult> getAll() {
        return voteResultDao.getAll();
    }

    @Override
    public Optional<VoteResult> get(Long voteResultId) {
        return voteResultDao.get(voteResultId);
    }

    @Override
    public void update(Long voteResultId) {
        Optional<VoteResult> voteResultOptional = voteResultDao.get(voteResultId);
        if (voteResultOptional.isPresent()) {
            VoteResult voteResult = voteResultOptional.get();
            voteResultDao.update(voteResult);
        }
    }

    @Override
    public void delete(VoteResult voteResult) {
        voteResultDao.delete(voteResult);
    }

    @Override
    public boolean hasUserVoted(Long userId, Long votingId) {
        return voteResultDao.hasUserVoted(userId, votingId);
    }

    @Override
    public List<VoteResult> getByVotingId(Long votingId) {
        return voteResultDao.getByVotingId(votingId);
    }

    @Override
    public ShowVotingResultsDto getShowVotingResults(Long votingId) {
        ShowVotingResultsDto showVotingResults = new ShowVotingResultsDto();
        showVotingResults.setUserPublicIds(votingDao.getPublicIds(votingId));

        List<VoteResult> voteResults = voteResultDao.getByVotingId(votingId);
        int totalVotes = voteResults.size();
        Map<Candidate, Integer> candidateVotesMap = new HashMap<>();

        for (VoteResult voteResult : voteResults) {
            Candidate candidate = voteResult.getCandidate();
            candidateVotesMap.put(candidate, candidateVotesMap.getOrDefault(candidate, 0) + 1);
        }

        Voting voting = votingDao.get(votingId).orElseThrow(() -> new RuntimeException("Voting not found"));
        List<Candidate> allCandidates = voting.getCandidates();
        List<CandidatePercentageDto> candidatePercentageDtos = new ArrayList<>();

        for (Candidate candidate : allCandidates) {
            int voteCount = candidateVotesMap.getOrDefault(candidate, 0);
            int percentage = totalVotes > 0 ? (int) ((voteCount / (double) totalVotes) * 100) : 0;

            CandidatePercentageDto candidatePercentageDto = new CandidatePercentageDto();
            candidatePercentageDto.setCandidate(candidate);
            candidatePercentageDto.setPercentage(percentage);
            candidatePercentageDto.setVoteAmount(voteCount);
            candidatePercentageDtos.add(candidatePercentageDto);
        }

        showVotingResults.setCandidatePercentages(candidatePercentageDtos);
        return showVotingResults;
    }
}
