package dev.civicpulse.platformconfig.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "countries")
public class CountryJpaEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private String name;

  // schema.sql declares this char(2) (fixed-length bpchar), not varchar — the JDBC driver
  // reports Types#CHAR, not Types#VARCHAR, which Hibernate infers by default for String
  // fields (see identity-service's citext fix for the same class of validation mismatch).
  @Column(nullable = false, unique = true, length = 2)
  @JdbcTypeCode(SqlTypes.CHAR)
  private String code;

  protected CountryJpaEntity() {}

  public CountryJpaEntity(UUID id, String name, String code) {
    this.id = id;
    this.name = name;
    this.code = code;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getCode() {
    return code;
  }
}
