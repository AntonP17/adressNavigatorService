package by.antohakon.adressnavigatorservice.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.time.Instant;

@ControllerAdvice
@Slf4j
@Data
public class GlobalExceptionHandler {

    @Data
    @AllArgsConstructor
    private static class ErrorResponse {
        private String errorType;
        private String message;
        private Instant timestamp;
    }


    @ExceptionHandler(DuplicateAdressException.class)
    public ResponseEntity<ErrorResponse> handleDublicateException(final RuntimeException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        exception.getClass().getSimpleName(),
                        exception.getMessage(),
                        Instant.now()
                ));
    }


    @ExceptionHandler({IOException.class, InterruptedException.class})
    public ResponseEntity<ErrorResponse> handleHttpRequestException(Exception ex) {
        log.error("Ошибка при выполнении HTTP-запроса: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        ex.getClass().getSimpleName(),
                        ex.getMessage(),
                        Instant.now()
                ));
    }

    // Обработка ошибок парсинга JSON
    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorResponse> handleJsonProcessingException(JsonProcessingException ex) {
        log.error("Ошибка парсинга JSON: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        ex.getClass().getSimpleName(),
                        ex.getMessage(),
                        Instant.now()
                ));
    }

}
