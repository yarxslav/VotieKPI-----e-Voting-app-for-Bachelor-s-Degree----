package ua.kpi.votieapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class CandidateImagesDto {
    private List<String> candidateIds;
    private List<String> images;
}