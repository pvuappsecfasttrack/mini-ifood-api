package com.marcosdias.miniifood.user.web;

import com.marcosdias.miniifood.user.service.exception.EmailAlreadyInUseException;
import com.marcosdias.miniifood.user.service.exception.ResourceNotFoundException;
import com.marcosdias.miniifood.user.web.dto.ApiError;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        ApiError error = new ApiError(
            HttpStatus.NOT_FOUND.value(),
            "Resource not found",
            OffsetDateTime.now(),
            List.of(ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiError> handleConflict(EmailAlreadyInUseException ex) {
        ApiError error = new ApiError(
            HttpStatus.CONFLICT.value(),
            "Business validation error",
            OffsetDateTime.now(),
            List.of(ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .toList();

        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Validation error",
            OffsetDateTime.now(),
            details
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        ApiError error = new ApiError(
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication error",
            OffsetDateTime.now(),
            List.of("Invalid email or password")
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}

