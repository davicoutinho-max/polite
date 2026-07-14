package dev.civicpulse.platformconfig.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "states")
public class StateJpaEntity {

  @Id private UUID id;

  @Column(name = "country_id", nullable = false)
  private UUID countryId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String code;

  protected StateJpaEntity() {}

  public StateJpaEntity(UUID id, UUID countryId, String name, String code) {
    this.id = id;
    this.countryId = countryId;
    this.name = name;
    this.code = code;
  }

  public UUID getId() {
    return id;
  }

  public UUID getCountryId() {
    return countryId;
  }

  public String getName() {
    return name;
  }

  public String getCode() {
    return code;
  }
}
