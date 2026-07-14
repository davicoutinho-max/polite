package dev.civicpulse.platformconfig.adapter.in.web;

import dev.civicpulse.platformconfig.domain.exception.CannotRemoveDefaultLanguageException;
import dev.civicpulse.platformconfig.domain.exception.CountryNotFoundException;
import dev.civicpulse.platformconfig.domain.exception.DuplicatePartyRegistrationException;
import dev.civicpulse.platformconfig.domain.exception.LanguageNotFoundException;
import dev.civicpulse.platformconfig.domain.exception.PartyRegistryEntryNotFoundException;
import dev.civicpulse.platformconfig.domain.exception.PoliticianAssignmentNotFoundException;
import dev.civicpulse.platformconfig.domain.exception.TranslationKeyNotFoundException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(PartyRegistryEntryNotFoundException.class)
  public ProblemDetail handlePartyNotFound(PartyRegistryEntryNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Party not found", ex.getMessage());
  }

  @ExceptionHandler(DuplicatePartyRegistrationException.class)
  public ProblemDetail handleDuplicateParty(DuplicatePartyRegistrationException ex) {
    return problem(HttpStatus.CONFLICT, "Party already registered", ex.getMessage());
  }

  @ExceptionHandler(PoliticianAssignmentNotFoundException.class)
  public ProblemDetail handleAssignmentNotFound(PoliticianAssignmentNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Assignment not found", ex.getMessage());
  }

  @ExceptionHandler(CountryNotFoundException.class)
  public ProblemDetail handleCountryNotFound(CountryNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Country not found", ex.getMessage());
  }

  @ExceptionHandler(LanguageNotFoundException.class)
  public ProblemDetail handleLanguageNotFound(LanguageNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Language not found", ex.getMessage());
  }

  @ExceptionHandler(CannotRemoveDefaultLanguageException.class)
  public ProblemDetail handleCannotRemoveDefault(CannotRemoveDefaultLanguageException ex) {
    return problem(HttpStatus.CONFLICT, "Cannot remove default language", ex.getMessage());
  }

  @ExceptionHandler(TranslationKeyNotFoundException.class)
  public ProblemDetail handleTranslationKeyNotFound(TranslationKeyNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Translation key not found", ex.getMessage());
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
