package org.example.mobilyecommerce.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ معالج خاص لـ UsernameNotFoundException - يرجع 404
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleUsernameNotFound(
            UsernameNotFoundException ex,
            HttpServletRequest request) {

        log.warn("⚠️ User not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorDetails(request, HttpStatus.NOT_FOUND,
                        "User not found. Please check your email and try again."));
    }

    // ✅ معالج عام لأخطاء Authentication - يرجع 401
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetails> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        log.warn("⚠️ Authentication failed: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildErrorDetails(request, HttpStatus.UNAUTHORIZED,
                        "Invalid credentials. Please check your password and try again."));
    }

    // ✅ معالج لأخطاء الصلاحيات من @PreAuthorize
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAuthorizationDenied(
            AuthorizationDeniedException ex,
            HttpServletRequest request) {

        log.warn("Authorization Denied: {} for path: {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildErrorDetails(request, HttpStatus.FORBIDDEN,
                        "You don't have permission to access this resource"));
    }

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

        log.warn("Not Found Exception: {}", ex.getMessage());

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
                .body(buildErrorDetails(request, HttpStatus.INTERNAL_SERVER_ERROR,
                        "Something went wrong. Please try again later."));
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