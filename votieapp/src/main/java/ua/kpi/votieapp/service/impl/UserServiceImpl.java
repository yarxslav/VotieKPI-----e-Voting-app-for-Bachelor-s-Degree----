package ua.kpi.votieapp.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ua.kpi.votieapp.config.AppConfig;
import ua.kpi.votieapp.dao.UserDao;
import ua.kpi.votieapp.dto.LoginDto;
import ua.kpi.votieapp.entity.User;
import ua.kpi.votieapp.entity.VerificationData;
import ua.kpi.votieapp.exception.EmailNotFoundException;
import ua.kpi.votieapp.exception.ImageDeletionForbidden;
import ua.kpi.votieapp.exception.InvalidFileTypeException;
import ua.kpi.votieapp.model.UserStatus;
import ua.kpi.votieapp.service.UserService;
import ua.kpi.votieapp.util.FileUtil;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void create(User user) {
        try {
            user.setVoterPublicId(UUID.randomUUID().toString());
            VerificationData verificationData = new VerificationData();
            verificationData.setUserStatus(UserStatus.NOT_VERIFIED);
            user.setVerificationData(verificationData);
            userDao.create(user);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
        }
    }

    @Override
    public Optional<User> get(Long userId) {
        return userDao.get(userId);
    }

    @Override
    public User login(LoginDto loginDto) {
        Optional<User> optionalUser = userDao.login(loginDto);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            logger.warn("Unauthorized login attempt for user: %s".formatted(loginDto.getLogin()));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid login or password");
        }
    }

    @Override
    public List<User> getAll() {
        return userDao.getAll();
    }

    @Override
    public UserStatus getUserStatus(Long userId) {
        return userDao.getUserStatus(userId);
    }

    @Override
    public User update(User user) {
        return userDao.update(user);
    }

    @Override
    public void delete(User user) {
        try {
            deleteImage(user.getId());
            userDao.delete(user);
        } catch (Exception e) {
            logger.error("Error deleting user with ID {}: {}", user.getId(), e.getMessage());
        }
    }

    @Override
    public void saveImage(byte[] image, User user) {
        String fileExtension = getFileExtension(image);

        String uploadDir = appConfig.getImagesPath();
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists() && !uploadDirFile.mkdirs()) {
            logger.error("Unable to create directory: {}", uploadDir);
            throw new RuntimeException("Unable to create directory");
        }

        String fileName = "%s.%s".formatted(UUID.randomUUID(), fileExtension);
        File file = new File("%s%s".formatted(uploadDir, fileName));

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(image);
        } catch (IOException e) {
            logger.error("Failed to save file for user ID {}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Failed to save file");
        }

        user.getVerificationData().setImageName(fileName);
        user.getVerificationData().setUserStatus(UserStatus.IN_PROGRESS);
        try {
            update(user);
        } catch (Exception e) {
            logger.error("Error updating user after saving image for user ID {}: {}", user.getId(), e.getMessage());
        }
    }

    @Override
    public byte[] getImage(Long userId) {
        String imageName = userDao.getImageName(userId);
        if (imageName == null) {
            return null;
        }

        try {
            return Files.readAllBytes(Paths.get("%s%s".formatted(appConfig.getImagesPath(), imageName)));
        } catch (IOException e) {
            logger.error("Error reading image file for user ID {}: {}", userId, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean deleteImage(Long userId) {
        if (userDao.getUserStatus(userId) == null || userDao.getUserStatus(userId) == UserStatus.VERIFIED) {
            throw new ImageDeletionForbidden("Image deletion is prohibited");
        }

        String imageName = userDao.getImageName(userId);
        if (imageName == null) {
            return false;
        }

        try {
            Files.delete(Paths.get(appConfig.getImagesPath(), imageName));
        } catch (IOException e) {
            logger.error("Error deleting image file for user ID {}: {}", userId, e.getMessage());
            return false;
        }

        Optional<User> optionalUser = userDao.get(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.getVerificationData().setImageName(null);
            user.getVerificationData().setUserStatus(UserStatus.NOT_VERIFIED);
            try {
                userDao.update(user);
                return true;
            } catch (Exception e) {
                logger.error("Error updating user after deleting image for user ID {}: {}", userId, e.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public void sendPassword(String email) {
        try {
            if (!userDao.isEmailExists(email)) {
                logger.info("Email not found: {}", email);
                throw new EmailNotFoundException("Email not found");
            }

            String password = userDao.getPassword(email);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("pwd-reminder@trial-o65qngkk75jgwr12.mlsender.net");
            message.setTo(email);
            message.setSubject("Запит на відновлення паролю");
            message.setText("Вітаємо! Нагадуємо Ваш пароль для входу: %s".formatted(password));

            mailSender.send(message);
            logger.info("Password sent to email: {}", email);
        } catch (Exception e) {
            logger.error("Error sending password to email {}: {}", email, e.getMessage());
        }
    }

    private String getFileExtension(byte[] image) {
        return switch (FileUtil.getFileType(image)) {
            case "jpg" -> "jpg";
            case "jpeg" -> "jpeg";
            case "png" -> "png";
            default -> throw new InvalidFileTypeException("Invalid file type");
        };
    }
}
