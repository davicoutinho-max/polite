package dev.civicpulse.participation.domain.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Survey {

  private final UUID id;
  private final String question;
  private final String context;

  private Survey(UUID id, String question, String context) {
    this.id = Objects.requireNonNull(id);
    this.question = requireNonBlank(question);
    this.context = context;
  }

  public static Survey create(UUID id, String question, String context) {
    return new Survey(id, question, context);
  }

  public static Survey reconstitute(UUID id, String question, String context) {
    return new Survey(id, question, context);
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("question must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public String question() {
    return question;
  }

  public Optional<String> context() {
    return Optional.ofNullable(context);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Survey other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
