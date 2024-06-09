package ua.kpi.votieapp.dto;

import lombok.Data;

@Data
public class VoteResultDto {
    private Long id;
    private Long votingId;
    private Long userId;
    private Long candidateId;
}
