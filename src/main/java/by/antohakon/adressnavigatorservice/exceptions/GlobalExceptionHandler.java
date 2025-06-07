package by.antohakon.adressnavigatorservice.exceptions;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.awt.geom.RectangularShape;
import java.time.Instant;

@ControllerAdvice
@Slf4j
@Data
public class GlobalExceptionHandler {

    private static class ErrorResponse {
        private String message;
        private Instant timestamp;
        private String errorType;
    }

public ResponseEntity<ErrorResponse> handleDublicateException(final Exception exception) {

}

}
