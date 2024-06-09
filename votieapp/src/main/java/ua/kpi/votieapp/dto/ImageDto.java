package ua.kpi.votieapp.dto;

import lombok.Data;

@Data
public class ImageDto {
    private Long id;
    private String image;

    public ImageDto(Long id, String image) {
        this.id = id;
        this.image = image;
    }
}

