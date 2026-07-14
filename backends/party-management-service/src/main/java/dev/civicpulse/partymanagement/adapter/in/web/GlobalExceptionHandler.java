package dev.civicpulse.partymanagement.adapter.in.web;

import dev.civicpulse.partymanagement.domain.exception.AffiliationRequestNotFoundException;
import dev.civicpulse.partymanagement.domain.exception.AffiliationRequestNotPendingException;
import dev.civicpulse.partymanagement.domain.exception.AlreadyRepresentativeException;
import dev.civicpulse.partymanagement.domain.exception.IdentityProvisioningException;
import dev.civicpulse.partymanagement.domain.exception.PartyMemberNotFoundException;
import dev.civicpulse.partymanagement.domain.exception.PartyProfileNotFoundException;
import dev.civicpulse.partymanagement.domain.exception.RepresentativeNotFoundException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(PartyProfileNotFoundException.class)
  public ProblemDetail handlePartyProfileNotFound(PartyProfileNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Party profile not found", ex.getMessage());
  }

  @ExceptionHandler(RepresentativeNotFoundException.class)
  public ProblemDetail handleRepresentativeNotFound(RepresentativeNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Representative not found", ex.getMessage());
  }

  @ExceptionHandler(AffiliationRequestNotFoundException.class)
  public ProblemDetail handleAffiliationRequestNotFound(AffiliationRequestNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Affiliation request not found", ex.getMessage());
  }

  @ExceptionHandler(PartyMemberNotFoundException.class)
  public ProblemDetail handlePartyMemberNotFound(PartyMemberNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Party member not found", ex.getMessage());
  }

  @ExceptionHandler(AlreadyRepresentativeException.class)
  public ProblemDetail handleAlreadyRepresentative(AlreadyRepresentativeException ex) {
    return problem(HttpStatus.CONFLICT, "Already linked", ex.getMessage());
  }

  @ExceptionHandler(AffiliationRequestNotPendingException.class)
  public ProblemDetail handleAffiliationRequestNotPending(AffiliationRequestNotPendingException ex) {
    return problem(HttpStatus.CONFLICT, "Affiliation request already decided", ex.getMessage());
  }

  @ExceptionHandler(IdentityProvisioningException.class)
  public ProblemDetail handleIdentityProvisioning(IdentityProvisioningException ex) {
    return problem(HttpStatus.BAD_GATEWAY, "Politician registration failed", ex.getMessage());
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
