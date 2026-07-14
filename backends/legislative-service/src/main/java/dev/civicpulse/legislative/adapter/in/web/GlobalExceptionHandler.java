package dev.civicpulse.legislative.adapter.in.web;

import dev.civicpulse.legislative.domain.exception.DossierNotFoundException;
import dev.civicpulse.legislative.domain.exception.InvalidStatusTransitionException;
import dev.civicpulse.legislative.domain.exception.LegislativeItemNotFoundException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DossierNotFoundException.class)
  public ProblemDetail handleDossierNotFound(DossierNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Dossier not found", ex.getMessage());
  }

  @ExceptionHandler(LegislativeItemNotFoundException.class)
  public ProblemDetail handleLegislativeItemNotFound(LegislativeItemNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Legislative item not found", ex.getMessage());
  }

  @ExceptionHandler(InvalidStatusTransitionException.class)
  public ProblemDetail handleInvalidStatusTransition(InvalidStatusTransitionException ex) {
    return problem(HttpStatus.CONFLICT, "Invalid status transition", ex.getMessage());
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
