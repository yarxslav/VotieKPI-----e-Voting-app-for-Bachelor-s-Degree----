package ua.kpi.votieapp.service;

import ua.kpi.votieapp.dto.LoginDto;
import ua.kpi.votieapp.entity.User;
import ua.kpi.votieapp.model.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserService {
    void create(User user);

    User login(LoginDto loginDto);

    Optional<User> get(Long userId);

    List<User> getAll();

    UserStatus getUserStatus(Long userId);

    User update(User user);

    void delete(User user);

    void saveImage(byte[] image, User user);

    byte[] getImage(Long userId);

    boolean deleteImage(Long userId);

    void sendPassword(String email);
}

