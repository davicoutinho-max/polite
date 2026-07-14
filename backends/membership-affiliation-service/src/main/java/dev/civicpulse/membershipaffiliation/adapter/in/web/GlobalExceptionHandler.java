package dev.civicpulse.membershipaffiliation.adapter.in.web;

import dev.civicpulse.membershipaffiliation.domain.exception.ActiveAffiliationAlreadyExistsException;
import dev.civicpulse.membershipaffiliation.domain.exception.AffiliationNotFoundException;
import dev.civicpulse.membershipaffiliation.domain.exception.FeeAlreadyPaidException;
import dev.civicpulse.membershipaffiliation.domain.exception.InvalidAffiliationTransitionException;
import dev.civicpulse.membershipaffiliation.domain.exception.MembershipFeeNotFoundException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AffiliationNotFoundException.class)
  public ProblemDetail handleAffiliationNotFound(AffiliationNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Affiliation not found", ex.getMessage());
  }

  @ExceptionHandler(ActiveAffiliationAlreadyExistsException.class)
  public ProblemDetail handleActiveAffiliationExists(ActiveAffiliationAlreadyExistsException ex) {
    return problem(HttpStatus.CONFLICT, "Affiliation already active", ex.getMessage());
  }

  @ExceptionHandler(InvalidAffiliationTransitionException.class)
  public ProblemDetail handleInvalidTransition(InvalidAffiliationTransitionException ex) {
    return problem(HttpStatus.CONFLICT, "Invalid affiliation transition", ex.getMessage());
  }

  @ExceptionHandler(MembershipFeeNotFoundException.class)
  public ProblemDetail handleFeeNotFound(MembershipFeeNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Membership fee not found", ex.getMessage());
  }

  @ExceptionHandler(FeeAlreadyPaidException.class)
  public ProblemDetail handleFeeAlreadyPaid(FeeAlreadyPaidException ex) {
    return problem(HttpStatus.CONFLICT, "Fee already paid", ex.getMessage());
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
