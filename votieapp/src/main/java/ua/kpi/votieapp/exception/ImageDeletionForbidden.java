package ua.kpi.votieapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ImageDeletionForbidden extends RuntimeException {
    public ImageDeletionForbidden (String message) {
        super(message);
    }
}
