package org.example.mobilyecommerce.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // validation errors from @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .reduce((m1, m2) -> m1 + "; " + m2)
                .orElse("Validation failed");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorDetails(request, HttpStatus.BAD_REQUEST,
                        errorMessage));
    }
//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<ErrorDetails> handleConstraintViolation(
//            ConstraintViolationException ex,
//            HttpServletRequest request) {
//
//        String errorMessage = ex.getConstraintViolations().stream()
//                .map(cv -> cv.getMessage())
//                .reduce((m1, m2) -> m1 + "; " + m2)
//                .orElse("Validation failed");
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(buildErrorDetails(request, HttpStatus.BAD_REQUEST, errorMessage));
//    }
    @ExceptionHandler({
            UserAlreadyExistsException.class,
    })
    public ResponseEntity<ErrorDetails> handleConflictExceptions(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Conflict Exception: {}", ex.getMessage());


        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorDetails(request, HttpStatus.CONFLICT, ex.getMessage()));
    }
    @ExceptionHandler({
        TokenNotFoundException.class,
            UserNotFoundException.class
    })
    public ResponseEntity<ErrorDetails> handleNotFoundExceptions(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Conflict Exception: {}", ex.getMessage());


        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorDetails(request, HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler({
            InvalidCredentialsException.class,
            TokenExpiredException.class
    })
    public ResponseEntity<ErrorDetails> handleBadRequestExceptions(
            RuntimeException ex,
            HttpServletRequest request
    ){
        log.warn("Bad Request: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorDetails(request, HttpStatus.BAD_REQUEST, ex.getMessage()));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleAllExceptions(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorDetails(request, HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong"));
    }

    // unified error response - النسخة الأساسية
    private ErrorDetails buildErrorDetails(HttpServletRequest request,
                                           HttpStatus status,
                                           String message) {
        return buildErrorDetails(request, status, message, message);
    }

    // unified error response - النسخة مع التفاصيل
    private ErrorDetails buildErrorDetails(HttpServletRequest request,
                                           HttpStatus status,
                                           String message,
                                           String details) {
        ErrorDetails errorResponse = new ErrorDetails();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(status.value());
        errorResponse.setError(status.getReasonPhrase());
        errorResponse.setMessage(message);
        errorResponse.setPath(request.getRequestURI());
        return errorResponse;
    }
}
