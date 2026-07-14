package dev.civicpulse.payments.domain.exception;

public final class DuplicateIdempotencyKeyException extends RuntimeException {

  public DuplicateIdempotencyKeyException(String idempotencyKey) {
    super("A payment intent with idempotency key '" + idempotencyKey + "' already exists");
  }
}
