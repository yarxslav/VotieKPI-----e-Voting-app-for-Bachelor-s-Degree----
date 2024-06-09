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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.kpi.votieapp.dto.ShowVotingResultsDto;
import ua.kpi.votieapp.dto.VoteResultDto;
import ua.kpi.votieapp.entity.VoteResult;
import ua.kpi.votieapp.exception.InvalidFileTypeException;
import ua.kpi.votieapp.exception.VotingDeletionException;
import ua.kpi.votieapp.model.ExceptionResponse;
import ua.kpi.votieapp.service.VoteResultService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/vote-results")
public class VoteResultController {
    private static final Integer UNPROCESSABLE_ENTITY = 422;
    private static final Integer BAD_REQUEST = 400;
    private static final Integer INTERNAL_SERVER_ERROR = 500;

    private static final Logger logger = LoggerFactory.getLogger(VoteResultController.class);

    @Autowired
    private VoteResultService voteResultService;

    @PostMapping
    public ResponseEntity<Void> createVoteResult(@RequestBody VoteResultDto voteResultDto) {
        voteResultService.create(voteResultDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<VoteResult>> getAll() {
        List<VoteResult> voteResults = voteResultService.getAll();
        return ResponseEntity.ok(voteResults);
    }

    @GetMapping("/{voteResultId}")
    public ResponseEntity<VoteResult> get(@PathVariable("voteResultId") Long voteResultId) {
        Optional<VoteResult> voteResult = voteResultService.get(voteResultId);
        return voteResult.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}/{votingId}")
    public ResponseEntity<Boolean> hasUserVoted(@PathVariable("userId") Long userId,
                                                @PathVariable("votingId") Long votingId) {
        boolean hasVoted = voteResultService.hasUserVoted(userId, votingId);
        return ResponseEntity.ok(hasVoted);
    }

    @GetMapping("/votings/{votingId}")
    public ResponseEntity<List<VoteResult>> getByVotingId(@PathVariable("votingId") Long votingId) {
        List<VoteResult> voteResults = voteResultService.getByVotingId(votingId);
        return ResponseEntity.ok(voteResults);
    }

    @GetMapping("/show/{votingId}")
    public ResponseEntity<ShowVotingResultsDto> getShowVotingResults(@PathVariable("votingId") Long votingId) {
        ShowVotingResultsDto showVotingResults = voteResultService.getShowVotingResults(votingId);
        return ResponseEntity.ok(showVotingResults);
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
        logger.error("Failed to delete the vote result: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(getExceptionResponse(e.getMessage(), INTERNAL_SERVER_ERROR));
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
