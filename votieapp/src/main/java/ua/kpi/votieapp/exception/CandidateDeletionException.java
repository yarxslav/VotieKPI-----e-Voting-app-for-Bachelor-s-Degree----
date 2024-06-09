package ua.kpi.votieapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CandidateDeletionException extends RuntimeException {
    public CandidateDeletionException(String message) {
        super(message);
    }
}
