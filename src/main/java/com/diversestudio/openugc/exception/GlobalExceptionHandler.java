package com.diversestudio.openugc.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Translates the API's custom exceptions into a single, consistent error body.
 * <p>
 * Every handler returns an RFC 9457 {@link ProblemDetail}, served as
 * {@code application/problem+json}, e.g.:
 * <pre>
 * {
 *   "type": "about:blank",
 *   "title": "Bad Request",
 *   "status": 400,
 *   "detail": "Invalid sort field: '...'",
 *   "timestamp": "2026-11-06T12:00:00Z"
 * }
 * </pre>
 * Spring's own built-in errors (404 no route, 405, malformed body, ...) use the same
 * shape: they are handled by {@link ResponseEntityExceptionHandler}, which this class extends
 * (Spring Boot's own ProblemDetail handler backs off when one is present).
 * <p>
 * A failed {@code @Valid} check returns 400 with an {@code errors} map of field to message, e.g.
 * {@code "errors": {"username": "size must be between 3 and 32"}}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Builds a ProblemDetail with a shared {@code timestamp} property for a uniform body. */
    private ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ProblemDetail handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ProblemDetail handleUnauthorizedActionException(UnauthorizedActionException ex) {
        return problem(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ProblemDetail handleInvalidPasswordException(InvalidPasswordException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ProblemDetail handleInvalidRequestException(InvalidRequestException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Failed login: the caller is not authenticated, so 401 (403 means "authenticated but not allowed"). */
    @ExceptionHandler(IncorrectPasswordException.class)
    public ProblemDetail handleIncorrectPasswordException(IncorrectPasswordException ex) {
        return problem(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /** Safety net for JPA lookups (e.g. getReferenceById) that miss. */
    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFoundException(EntityNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * A unique or foreign key constraint was violated, e.g. two requests racing for the same username.
     * The database message names tables and columns, so it is logged and not returned.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        logger.debug("Data integrity violation", ex);
        return problem(HttpStatus.CONFLICT, "The request conflicts with existing data");
    }

    /** {@code @Valid} failed on a request body or part: 400 listing every invalid field. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ProblemDetail body = ex.getBody();
        body.setDetail("Request validation failed");
        body.setProperty("errors", errors);
        body.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).headers(headers).body(body);
    }
}
