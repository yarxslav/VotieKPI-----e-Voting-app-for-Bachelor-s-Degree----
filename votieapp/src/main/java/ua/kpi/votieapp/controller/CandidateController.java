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

import ua.kpi.votieapp.dto.ImageDto;
import ua.kpi.votieapp.entity.Candidate;
import ua.kpi.votieapp.exception.EmailNotFoundException;
import ua.kpi.votieapp.exception.ImageDeletionForbidden;
import ua.kpi.votieapp.exception.InvalidFileTypeException;
import ua.kpi.votieapp.exception.UserDeletionException;
import ua.kpi.votieapp.model.ExceptionResponse;
import ua.kpi.votieapp.service.CandidateService;
import ua.kpi.votieapp.util.FileUtil;

@RestController
@RequestMapping("/candidates")
public class CandidateController {
    private static final Integer UNPROCESSABLE_ENTITY = 422;
    private static final Integer BAD_REQUEST = 400;
    private static final Integer INTERNAL_SERVER_ERROR = 500;

    private static final Logger logger = LoggerFactory.getLogger(CandidateController.class);

    @Autowired
    private CandidateService candidateService;

    @PostMapping
    public ResponseEntity<Candidate> create(@RequestBody Candidate candidate) {
        candidateService.create(candidate);
        return ResponseEntity.status(HttpStatus.CREATED).body(candidate);
    }

    @GetMapping
    public ResponseEntity<List<Candidate>> getAll() {
        List<Candidate> candidates = candidateService.getAll();
        return ResponseEntity.ok(candidates);
    }

    @GetMapping("/{candidateId}")
    public ResponseEntity<Candidate> get(@PathVariable("candidateId") Long candidateId) {
        Optional<Candidate> candidate = candidateService.get(candidateId);
        return candidate.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{candidateId}")
    public ResponseEntity<Candidate> update(@PathVariable("candidateId") Long candidateId, @RequestBody Candidate candidate) {
        Optional<Candidate> updatingCandidate = candidateService.get(candidateId);
        if (updatingCandidate.isPresent()) {
            candidate.setId(candidateId);
            Candidate updatedCandidate = candidateService.update(candidate);
            return ResponseEntity.ok(updatedCandidate);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{candidateId}")
    public ResponseEntity<String> delete(@PathVariable("candidateId") Long candidateId) {
        Optional<Candidate> candidate = candidateService.get(candidateId);
        if (candidate.isPresent()) {
            candidateService.delete(candidate.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{candidateId}/image")
    public ResponseEntity<String> uploadImage(@PathVariable("candidateId") Long candidateId, @RequestBody byte[] image) {
        Optional<Candidate> candidate = candidateService.get(candidateId);
        if (candidate.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (image.length == 0) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        candidateService.saveImage(image, candidate.get());
        return ResponseEntity.ok("File uploaded successfully");
    }

    @GetMapping("/{candidateId}/image")
    public ResponseEntity<Resource> getImage(@PathVariable("candidateId") Long candidateId) {
        try {
            byte[] imageResource = candidateService.getImage(candidateId);
            if (imageResource == null) {
                return ResponseEntity.noContent().build();
            }

            String fileType = FileUtil.getFileType(imageResource);
            String contentType = FileUtil.getContentType(fileType);
            String filename = "image_%d.%s".formatted(candidateId, fileType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(filename))
                    .body(new ByteArrayResource(imageResource));
        } catch (Exception e) {
            logger.error("Error getting image for candidate ID {}: {}", candidateId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/images")
    public ResponseEntity<List<ImageDto>> getAllImages() {
        try {
            List<ImageDto> images = candidateService.getAllImages();
            if (images == null || images.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            logger.error("Error getting images for candidates: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{candidateId}/image")
    public ResponseEntity<Void> deleteImage(@PathVariable("candidateId") Long candidateId) {
        try {
            boolean isDeleted = candidateService.deleteImage(candidateId);
            if (isDeleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (ImageDeletionForbidden e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error deleting image for candidate ID {}: {}", candidateId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
        logger.error("Failed to delete the candidate: {}", e.getMessage());
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
