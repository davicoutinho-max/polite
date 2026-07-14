package dev.civicpulse.identity.adapter.in.web;

import dev.civicpulse.identity.domain.exception.AccountNotFoundException;
import dev.civicpulse.identity.domain.exception.DuplicateAccountException;
import dev.civicpulse.identity.domain.exception.InvalidCredentialsException;
import dev.civicpulse.identity.domain.exception.InvalidDocumentNumberException;
import dev.civicpulse.identity.domain.exception.SessionNotActiveException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps domain exceptions to RFC 7807 problem-detail responses — the frontend/Gateway never
 * has to pattern-match on a message string to know what went wrong. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DuplicateAccountException.class)
  public ProblemDetail handleDuplicate(DuplicateAccountException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problem.setTitle("Account already exists");
    problem.setProperty("field", ex.field());
    return problem;
  }

  @ExceptionHandler(InvalidDocumentNumberException.class)
  public ProblemDetail handleInvalidDocument(InvalidDocumentNumberException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problem.setTitle("Invalid document number");
    return problem;
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    problem.setTitle("Authentication failed");
    return problem;
  }

  @ExceptionHandler(SessionNotActiveException.class)
  public ProblemDetail handleSessionNotActive(SessionNotActiveException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    problem.setTitle("Session not active");
    return problem;
  }

  @ExceptionHandler(AccountNotFoundException.class)
  public ProblemDetail handleAccountNotFound(AccountNotFoundException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problem.setTitle("Account not found");
    return problem;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    String detail =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.joining("; "));
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    problem.setTitle("Validation failed");
    return problem;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problem.setTitle("Invalid request");
    return problem;
  }
}
