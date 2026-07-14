package dev.civicpulse.fundraising.adapter.in.web;

import dev.civicpulse.fundraising.domain.exception.FundraiserNotFoundException;
import dev.civicpulse.fundraising.domain.exception.LedgerNotPublicException;
import dev.civicpulse.fundraising.domain.exception.PaymentIntentLookupException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(FundraiserNotFoundException.class)
  public ProblemDetail handleFundraiserNotFound(FundraiserNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Fundraiser not found", ex.getMessage());
  }

  @ExceptionHandler(LedgerNotPublicException.class)
  public ProblemDetail handleLedgerNotPublic(LedgerNotPublicException ex) {
    return problem(HttpStatus.FORBIDDEN, "Ledger not public", ex.getMessage());
  }

  @ExceptionHandler(PaymentIntentLookupException.class)
  public ProblemDetail handlePaymentIntentLookup(PaymentIntentLookupException ex) {
    return problem(HttpStatus.BAD_GATEWAY, "Payments Service lookup failed", ex.getMessage());
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
