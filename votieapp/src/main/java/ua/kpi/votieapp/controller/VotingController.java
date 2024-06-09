package ua.kpi.votieapp.controller;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ua.kpi.votieapp.dto.SearchDto;
import ua.kpi.votieapp.entity.Voting;
import ua.kpi.votieapp.exception.InvalidFileTypeException;
import ua.kpi.votieapp.exception.VotingDeletionException;
import ua.kpi.votieapp.model.ExceptionResponse;
import ua.kpi.votieapp.service.VotingService;

@RestController
@RequestMapping("/votings")
public class VotingController {
    private static final Integer UNPROCESSABLE_ENTITY = 422;
    private static final Integer BAD_REQUEST = 400;
    private static final Integer INTERNAL_SERVER_ERROR = 500;

    private static final Logger logger = LoggerFactory.getLogger(VotingController.class);

    @Autowired
    private VotingService votingService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Voting> createVoting(@RequestPart("voting") String votingString,
                                               @RequestPart("file") MultipartFile[] imageFile) {
        Voting voting = votingService.create(votingString, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(voting);
    }

    @GetMapping
    public ResponseEntity<List<Voting>> getAll() {
        List<Voting> votings = votingService.getAll();
        return ResponseEntity.ok(votings);
    }

    @GetMapping("/{votingId}")
    public ResponseEntity<Voting> get(@PathVariable("votingId") Long votingId) {
        Optional<Voting> voting = votingService.get(votingId);
        return voting.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{votingId}/publicIds")
    public ResponseEntity<List<String>> getPublicIds(@PathVariable("votingId") Long votingId) {
        List<String> publicIds = votingService.getPublicIds(votingId);
        return publicIds != null && !publicIds.isEmpty()
                ? ResponseEntity.ok(publicIds)
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/search")
    public ResponseEntity<List<Voting>> searchByName(@RequestBody SearchDto searchDto) {
        List<Voting> votings = votingService.searchByName(searchDto.getSearchString());
        if (votings == null || votings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(votings);
    }

    @PostMapping("/search-adm")
    public ResponseEntity<List<Voting>> searchByPublicId(@RequestBody SearchDto searchDto) {
        List<Voting> votings = votingService.searchByPublicId(searchDto.getSearchString());
        if (votings == null || votings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(votings);
    }

    @PutMapping(value = "/{votingId}")
    public ResponseEntity<Voting> updateVoting(@PathVariable("votingId") Long votingId,
                                               @RequestBody Map<String, Object> payload) {
        Voting updatedVoting = votingService.update(votingId, payload);
        return ResponseEntity.ok(updatedVoting);
    }

    @DeleteMapping("/{votingId}")
    public ResponseEntity<String> deleteVoting(@PathVariable("votingId") Long votingId) {
        Optional<Voting> voting = votingService.get(votingId);
        if (voting.isPresent()) {
            votingService.delete(voting.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
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

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidFileTypeException(InvalidFileTypeException e) {
        logger.error("Invalid file type exception: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(getExceptionResponse(e.getMessage(), BAD_REQUEST));
    }

    @ExceptionHandler(VotingDeletionException.class)
    public ResponseEntity<ExceptionResponse> handleVotingDeletionException(VotingDeletionException e) {
        logger.error("Failed to delete the voting: {}", e.getMessage());
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
