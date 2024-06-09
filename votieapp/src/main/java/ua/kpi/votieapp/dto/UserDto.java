package ua.kpi.votieapp.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UserDto {
    @NotEmpty
    private String name;

    @NotEmpty
    private String surname;

    @NotEmpty
    private String email;

    @NotEmpty
    private String login;

    @NotEmpty
    private String password;

    @NotEmpty
    private String confirmPassword;
}
