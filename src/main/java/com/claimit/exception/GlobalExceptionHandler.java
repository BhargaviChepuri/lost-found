
package com.claimit.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ItemNotFoundException and returns a custom response.
     * 
     * @param ex The ItemNotFoundException instance.
     * @return ResponseEntity with a message.
     */
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<?> handleItemNotFoundException(ItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", ex.getMessage()));
    }

    /**
     * Handles any other exception and returns a generic error response.
     * 
     * @param ex The Exception instance.
     * @return ResponseEntity with a generic error message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(Map.of("message", "An error occurred. Please try again later."));
    }
}

