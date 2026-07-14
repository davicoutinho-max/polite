package dev.civicpulse.privacycompliance.adapter.in.web;

import dev.civicpulse.privacycompliance.domain.exception.AccountDeletionRequestNotFoundException;
import dev.civicpulse.privacycompliance.domain.exception.DataExportRequestNotFoundException;
import dev.civicpulse.privacycompliance.domain.exception.InvalidDeletionTransitionException;
import dev.civicpulse.privacycompliance.domain.exception.InvalidExportTransitionException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DataExportRequestNotFoundException.class)
  public ProblemDetail handleDataExportRequestNotFound(DataExportRequestNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Data export request not found", ex.getMessage());
  }

  @ExceptionHandler(AccountDeletionRequestNotFoundException.class)
  public ProblemDetail handleAccountDeletionRequestNotFound(AccountDeletionRequestNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Account deletion request not found", ex.getMessage());
  }

  @ExceptionHandler(InvalidExportTransitionException.class)
  public ProblemDetail handleInvalidExportTransition(InvalidExportTransitionException ex) {
    return problem(HttpStatus.CONFLICT, "Invalid export transition", ex.getMessage());
  }

  @ExceptionHandler(InvalidDeletionTransitionException.class)
  public ProblemDetail handleInvalidDeletionTransition(InvalidDeletionTransitionException ex) {
    return problem(HttpStatus.CONFLICT, "Invalid deletion transition", ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    String detail =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.joining("; "));
    return problem(HttpStatus.BAD_REQUEST, "Validation failed", detail);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
    return problem(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage());
  }

  private static ProblemDetail problem(HttpStatus status, String title, String detail) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    return problem;
  }
}
