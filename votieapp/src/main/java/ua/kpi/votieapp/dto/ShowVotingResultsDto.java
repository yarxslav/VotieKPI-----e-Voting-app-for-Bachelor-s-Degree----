package ua.kpi.votieapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShowVotingResultsDto {
    private List<CandidatePercentageDto> candidatePercentages;
    private List<String> userPublicIds;
}
