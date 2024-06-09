package ua.kpi.votieapp.dao;

import ua.kpi.votieapp.dto.LoginDto;
import ua.kpi.votieapp.entity.User;
import ua.kpi.votieapp.model.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    void create(User user);

    Optional<User> get(Long userId);

    Optional<User> login(LoginDto loginDto);

    List<User> getAll();

    User update(User user);

    void delete(User user);

    String getImageName(Long userId);

    String getPassword(String email);

    UserStatus getUserStatus(Long userId);

    boolean isEmailExists(String email);

    boolean isUsernameExists(String login);
}
