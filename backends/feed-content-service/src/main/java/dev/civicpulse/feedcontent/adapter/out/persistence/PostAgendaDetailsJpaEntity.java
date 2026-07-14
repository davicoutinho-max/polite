package dev.civicpulse.feedcontent.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "post_agenda_details")
public class PostAgendaDetailsJpaEntity {

  @Id
  @Column(name = "post_id")
  private UUID postId;

  @Column(nullable = false)
  private String title;

  @Column(name = "event_date", nullable = false)
  private String eventDate;

  @Column(nullable = false)
  private String location;

  protected PostAgendaDetailsJpaEntity() {}

  public PostAgendaDetailsJpaEntity(UUID postId, String title, String eventDate, String location) {
    this.postId = postId;
    this.title = title;
    this.eventDate = eventDate;
    this.location = location;
  }

  public UUID getPostId() {
    return postId;
  }

  public String getTitle() {
    return title;
  }

  public String getEventDate() {
    return eventDate;
  }

  public String getLocation() {
    return location;
  }
}
