package dev.civicpulse.legislative.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "transparency_metrics")
public class TransparencyMetricJpaEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "politician_account_id", nullable = false)
  private UUID politicianAccountId;

  private String icon;

  @Column(nullable = false)
  private String label;

  @Column(name = "value_cents", nullable = false)
  private long valueCents;

  private String caption;

  @Column(nullable = false)
  private String period;

  protected TransparencyMetricJpaEntity() {}

  public TransparencyMetricJpaEntity(
      UUID id, UUID politicianAccountId, String icon, String label, long valueCents, String caption, String period) {
    this.id = id;
    this.politicianAccountId = politicianAccountId;
    this.icon = icon;
    this.label = label;
    this.valueCents = valueCents;
    this.caption = caption;
    this.period = period;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public String getIcon() {
    return icon;
  }

  public String getLabel() {
    return label;
  }

  public long getValueCents() {
    return valueCents;
  }

  public String getCaption() {
    return caption;
  }

  public String getPeriod() {
    return period;
  }
}
