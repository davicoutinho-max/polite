package dev.civicpulse.feedcontent.domain.exception;

public final class AlreadyLikedException extends RuntimeException {

  public AlreadyLikedException() {
    super("This post is already liked by this account");
  }
}
