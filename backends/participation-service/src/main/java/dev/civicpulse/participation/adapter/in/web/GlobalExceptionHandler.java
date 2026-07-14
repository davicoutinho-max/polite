package dev.civicpulse.participation.adapter.in.web;

import dev.civicpulse.participation.domain.exception.AlreadySignedException;
import dev.civicpulse.participation.domain.exception.AlreadyVotedException;
import dev.civicpulse.participation.domain.exception.ConsultationNotFoundException;
import dev.civicpulse.participation.domain.exception.PetitionNotFoundException;
import dev.civicpulse.participation.domain.exception.SurveyNotFoundException;
import dev.civicpulse.participation.domain.exception.SurveyOptionNotFoundException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(PetitionNotFoundException.class)
  public ProblemDetail handlePetitionNotFound(PetitionNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Petition not found", ex.getMessage());
  }

  @ExceptionHandler(AlreadySignedException.class)
  public ProblemDetail handleAlreadySigned(AlreadySignedException ex) {
    return problem(HttpStatus.CONFLICT, "Already signed", ex.getMessage());
  }

  @ExceptionHandler(ConsultationNotFoundException.class)
  public ProblemDetail handleConsultationNotFound(ConsultationNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Consultation not found", ex.getMessage());
  }

  @ExceptionHandler(SurveyNotFoundException.class)
  public ProblemDetail handleSurveyNotFound(SurveyNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Survey not found", ex.getMessage());
  }

  @ExceptionHandler(SurveyOptionNotFoundException.class)
  public ProblemDetail handleSurveyOptionNotFound(SurveyOptionNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Survey option not found", ex.getMessage());
  }

  @ExceptionHandler(AlreadyVotedException.class)
  public ProblemDetail handleAlreadyVoted(AlreadyVotedException ex) {
    return problem(HttpStatus.CONFLICT, "Already voted", ex.getMessage());
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
