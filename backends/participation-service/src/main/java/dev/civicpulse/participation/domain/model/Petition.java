package dev.civicpulse.participation.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** No framework imports — the domain core of the hexagonal architecture (see
 * docs/architecture/system-architecture.html). */
public final class Petition {

  private final UUID id;
  private final String title;
  private final String summary;
  private final String category;
  private final int goal;
  private int signaturesCount;
  private final LocalDate deadline;
  private final String imageUrl;
  private final String videoUrl;
  private final String fileUrl;
  private final String fileName;
  private final PetitionType petitionType;

  private Petition(
      UUID id,
      String title,
      String summary,
      String category,
      int goal,
      int signaturesCount,
      LocalDate deadline,
      String imageUrl,
      String videoUrl,
      String fileUrl,
      String fileName,
      PetitionType petitionType) {
    this.id = Objects.requireNonNull(id);
    this.title = requireNonBlank(title);
    this.summary = summary;
    this.category = category;
    if (goal <= 0) {
      throw new IllegalArgumentException("goal must be positive");
    }
    this.goal = goal;
    this.signaturesCount = signaturesCount;
    this.deadline = deadline;
    this.imageUrl = imageUrl;
    this.videoUrl = videoUrl;
    this.fileUrl = fileUrl;
    this.fileName = fileName;
    this.petitionType = Objects.requireNonNull(petitionType);
  }

  public static Petition create(
      UUID id,
      String title,
      String summary,
      String category,
      int goal,
      LocalDate deadline,
      String imageUrl,
      String videoUrl,
      String fileUrl,
      String fileName,
      PetitionType petitionType) {
    return new Petition(id, title, summary, category, goal, 0, deadline, imageUrl, videoUrl, fileUrl, fileName, petitionType);
  }

  public static Petition reconstitute(
      UUID id,
      String title,
      String summary,
      String category,
      int goal,
      int signaturesCount,
      LocalDate deadline,
      String imageUrl,
      String videoUrl,
      String fileUrl,
      String fileName,
      PetitionType petitionType) {
    return new Petition(id, title, summary, category, goal, signaturesCount, deadline, imageUrl, videoUrl, fileUrl, fileName, petitionType);
  }

  public void recordSignature() {
    signaturesCount++;
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public String title() {
    return title;
  }

  public Optional<String> summary() {
    return Optional.ofNullable(summary);
  }

  public Optional<String> category() {
    return Optional.ofNullable(category);
  }

  public int goal() {
    return goal;
  }

  public int signaturesCount() {
    return signaturesCount;
  }

  public Optional<LocalDate> deadline() {
    return Optional.ofNullable(deadline);
  }

  public Optional<String> imageUrl() {
    return Optional.ofNullable(imageUrl);
  }

  public Optional<String> videoUrl() {
    return Optional.ofNullable(videoUrl);
  }

  public Optional<String> fileUrl() {
    return Optional.ofNullable(fileUrl);
  }

  public Optional<String> fileName() {
    return Optional.ofNullable(fileName);
  }

  public PetitionType petitionType() {
    return petitionType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Petition other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
