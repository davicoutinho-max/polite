package dev.civicpulse.messaging.adapter.in.web;

import dev.civicpulse.messaging.domain.exception.ConversationNotFoundException;
import dev.civicpulse.messaging.domain.exception.NotAParticipantException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ConversationNotFoundException.class)
  public ProblemDetail handleConversationNotFound(ConversationNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Conversation not found", ex.getMessage());
  }

  @ExceptionHandler(NotAParticipantException.class)
  public ProblemDetail handleNotAParticipant(NotAParticipantException ex) {
    return problem(HttpStatus.FORBIDDEN, "Not a participant", ex.getMessage());
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
