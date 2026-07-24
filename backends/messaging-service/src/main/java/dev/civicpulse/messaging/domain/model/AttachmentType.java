package dev.civicpulse.messaging.domain.model;

public enum AttachmentType {
  IMAGE,
  VIDEO,
  AUDIO,
  FILE;

  public String code() {
    return name().toLowerCase();
  }

  public static AttachmentType fromCode(String code) {
    return valueOf(code.toUpperCase());
  }
}
