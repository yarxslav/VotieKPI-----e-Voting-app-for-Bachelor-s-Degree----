package ua.kpi.votieapp.dto;

import lombok.Data;
import ua.kpi.votieapp.entity.Candidate;

@Data
public class CandidatePercentageDto {
    private Candidate candidate;
    private Integer percentage;
    private Integer voteAmount;
}
