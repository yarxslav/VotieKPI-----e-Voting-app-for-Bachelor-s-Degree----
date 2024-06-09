package ua.kpi.votieapp.controller;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ua.kpi.votieapp.dto.EmailDto;
import ua.kpi.votieapp.dto.LoginDto;
import ua.kpi.votieapp.entity.User;
import ua.kpi.votieapp.exception.EmailNotFoundException;
import ua.kpi.votieapp.exception.ImageDeletionForbidden;
import ua.kpi.votieapp.exception.InvalidFileTypeException;
import ua.kpi.votieapp.exception.UserDeletionException;
import ua.kpi.votieapp.model.ExceptionResponse;
import ua.kpi.votieapp.model.UserStatus;
import ua.kpi.votieapp.service.UserService;
import ua.kpi.votieapp.util.FileUtil;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Integer UNPROCESSABLE_ENTITY = 422;
    private static final Integer BAD_REQUEST = 400;
    private static final Integer INTERNAL_SERVER_ERROR = 500;
    private static final Integer TEN_MB = 10485760;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginDto loginDto) {
        User user = userService.login(loginDto);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        List<User> users = userService.getAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> get(@PathVariable("userId") Long userId) {
        Optional<User> user = userService.get(userId);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}/status")
    public ResponseEntity<UserStatus> getUserStatus(@PathVariable("userId") Long userId) {
        UserStatus userStatus = userService.getUserStatus(userId);
        if (userStatus != null) {
            return ResponseEntity.ok(userStatus);
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable("userId") Long userId, @RequestBody User user) {
        Optional<User> updatingUser = userService.get(userId);
        if (updatingUser.isPresent()) {
            user.setId(userId);
            User updatedUser = userService.update(user);
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<User> partialUpdateUser(@PathVariable("userId") Long userId, @RequestBody User userUpdateData) {
        Optional<User> updatingUser = userService.get(userId);
        if (updatingUser.isPresent()) {
            User existingUser = updatingUser.get();
            existingUser.getVerificationData().setUserStatus(userUpdateData.getVerificationData().getUserStatus());
            if (userUpdateData.getVerificationData().getComment() != null) {
                existingUser.getVerificationData().setComment(userUpdateData.getVerificationData().getComment());
            }
            if (userUpdateData.getName() != null) {
                existingUser.setName(userUpdateData.getName());
            }
            if (userUpdateData.getSurname() != null) {
                existingUser.setSurname(userUpdateData.getSurname());
            }
            if (userUpdateData.getPatronymic() != null) {
                existingUser.setPatronymic(userUpdateData.getPatronymic());
            }
            if (userUpdateData.getUniversity() != null) {
                existingUser.setUniversity(userUpdateData.getUniversity());
            }
            if (userUpdateData.getFaculty() != null) {
                existingUser.setFaculty(userUpdateData.getFaculty());
            }
            if (userUpdateData.getGroup() != null) {
                existingUser.setGroup(userUpdateData.getGroup());
            }

            User updatedUser = userService.update(existingUser);
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable("userId") Long userId) {
        Optional<User> user = userService.get(userId);
        if (user.isPresent()) {
            userService.delete(user.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{userId}/image")
    public ResponseEntity<String> uploadUserImage(@PathVariable("userId") Long userId, @RequestBody byte[] image) {
        Optional<User> user = userService.get(userId);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (image.length == 0) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        if (image.length > TEN_MB) {
            return ResponseEntity.badRequest().body("The allowable file size is exceeded");
        }

        userService.saveImage(image, user.get());
        return ResponseEntity.ok("File uploaded successfully");
    }

    @GetMapping("/{userId}/image")
    public ResponseEntity<Resource> getUserImage(@PathVariable("userId") Long userId) {
        try {
            byte[] imageResource = userService.getImage(userId);
            if (imageResource == null) {
                return ResponseEntity.noContent().build();
            }

            String fileType = FileUtil.getFileType(imageResource);
            String contentType = FileUtil.getContentType(fileType);
            String filename = "image_%d.%s".formatted(userId, fileType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(filename))
                    .body(new ByteArrayResource(imageResource));
        } catch (Exception e) {
            logger.error("Error getting image for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{userId}/image")
    public ResponseEntity<Void> deleteUserImage(@PathVariable("userId") Long userId) {
        try {
            boolean isDeleted = userService.deleteImage(userId);
            if (isDeleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (ImageDeletionForbidden e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error deleting image for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/password")
    public ResponseEntity<String> sendPassword(@RequestBody EmailDto email) {
        userService.sendPassword(email.getEmail());
        return ResponseEntity.ok("Password sent successfully");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        logger.error("Validation error: {}", e.getMessage());
        BindingResult result = e.getBindingResult();
        List<String> details = result.getFieldErrors().stream()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
                .toList();
        return ResponseEntity.unprocessableEntity().body(getMultipleExceptionResponse(details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleNotReadableException(HttpMessageNotReadableException e) {
        logger.error("Not readable exception: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(getExceptionResponse("Invalid request body: %s".formatted(e.getMessage()), BAD_REQUEST));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolationException(ConstraintViolationException e) {
        logger.error("Constraint violation: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(getExceptionResponse("Invalid request body: %s".formatted(e.getMessage()), BAD_REQUEST));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        logger.error("Argument type mismatch: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(getExceptionResponse("Invalid argument: %s".formatted(e.getMessage()), BAD_REQUEST));
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ExceptionResponse> handleDataAccessResourceFailureException(DataAccessResourceFailureException e) {
        logger.error("Data access resource failure: {}", e.getMessage());
        return ResponseEntity.internalServerError().body(getExceptionResponse("Failed to connect to the database: %s"
                .formatted(e.getMessage()), INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleEmailNotFoundException(EmailNotFoundException e) {
        logger.info("Email not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(getExceptionResponse(e.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidFileTypeException(InvalidFileTypeException e) {
        logger.error("Invalid file type exception: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(getExceptionResponse(e.getMessage(), BAD_REQUEST));
    }

    @ExceptionHandler(UserDeletionException.class)
    public ResponseEntity<ExceptionResponse> handleUserDeletionException(UserDeletionException e) {
        logger.error("Failed to delete the user: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(getExceptionResponse(e.getMessage(), INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExceptionResponse> handleResponseStatusException(ResponseStatusException e) {
        logger.error("Response status exception: {}", e.getMessage());
        return ResponseEntity.status(e.getStatusCode())
                .body(getExceptionResponse(e.getReason(), e.getStatusCode().value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleOtherException(Exception e) {
        logger.error("Unexpected error: {}", e.getMessage());
        return ResponseEntity.internalServerError().body(getExceptionResponse("An error occurred: %s"
                .formatted(e.getMessage()), INTERNAL_SERVER_ERROR));
    }

    private ExceptionResponse getExceptionResponse(String detail, Integer code) {
        return ExceptionResponse.builder()
                .errors(Collections.singletonList(
                        ExceptionResponse.Error.builder()
                                .code(code)
                                .detail(detail)
                                .build()))
                .build();
    }

    private ExceptionResponse getMultipleExceptionResponse(List<String> details) {
        List<ExceptionResponse.Error> errorList = details.stream().map(detail -> ExceptionResponse.Error.builder()
                .code(UNPROCESSABLE_ENTITY)
                .detail(detail)
                .build()).toList();

        return ExceptionResponse.builder()
                .errors(errorList)
                .build();
    }
}
