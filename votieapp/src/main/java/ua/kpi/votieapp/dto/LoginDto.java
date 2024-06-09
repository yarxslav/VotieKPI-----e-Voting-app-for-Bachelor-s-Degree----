package ua.kpi.votieapp.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginDto {

    @NotEmpty
    private String login;

    @NotEmpty
    private String password;
}
