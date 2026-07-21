package dev.civicpulse.feedcontent.adapter.in.web;

import dev.civicpulse.feedcontent.domain.exception.AlreadyLikedException;
import dev.civicpulse.feedcontent.domain.exception.MediaUploadFailedException;
import dev.civicpulse.feedcontent.domain.exception.NotPostOwnerException;
import dev.civicpulse.feedcontent.domain.exception.PostNotFoundException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(PostNotFoundException.class)
  public ProblemDetail handlePostNotFound(PostNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Post not found", ex.getMessage());
  }

  @ExceptionHandler(AlreadyLikedException.class)
  public ProblemDetail handleAlreadyLiked(AlreadyLikedException ex) {
    return problem(HttpStatus.CONFLICT, "Already liked", ex.getMessage());
  }

  @ExceptionHandler(NotPostOwnerException.class)
  public ProblemDetail handleNotPostOwner(NotPostOwnerException ex) {
    return problem(HttpStatus.FORBIDDEN, "Not the post owner", ex.getMessage());
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

  @ExceptionHandler(MediaUploadFailedException.class)
  public ProblemDetail handleMediaUploadFailed(MediaUploadFailedException ex) {
    return problem(HttpStatus.BAD_GATEWAY, "Media upload failed", ex.getMessage());
  }

  private static ProblemDetail problem(HttpStatus status, String title, String detail) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    return problem;
  }
}
