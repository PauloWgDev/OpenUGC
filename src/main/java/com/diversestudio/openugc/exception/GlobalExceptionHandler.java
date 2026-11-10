package com.diversestudio.openugc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

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
 * shape thanks to {@code spring.mvc.problemdetails.enabled=true} in application.properties.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Builds a ProblemDetail with a shared {@code timestamp} property for a uniform body. */
    private ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ProblemDetail handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex) {
        // NOTE: 226 IM_USED is the pre-existing status for this case; left unchanged here.
        // It is almost certainly meant to be 409 CONFLICT - change it in a dedicated commit.
        return problem(HttpStatus.IM_USED, ex.getMessage());
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

    @ExceptionHandler(IncorrectPasswordException.class)
    public ProblemDetail handleIncorrectPasswordException(IncorrectPasswordException ex) {
        return problem(HttpStatus.FORBIDDEN, ex.getMessage());
    }
}
