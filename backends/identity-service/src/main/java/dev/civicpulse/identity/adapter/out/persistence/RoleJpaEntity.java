package dev.civicpulse.identity.adapter.out.persistence;

import dev.civicpulse.identity.domain.model.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class RoleJpaEntity {

  @Id private Short id;

  @Column(nullable = false, unique = true)
  private AccountType name;

  protected RoleJpaEntity() {}

  public RoleJpaEntity(Short id, AccountType name) {
    this.id = id;
    this.name = name;
  }

  public Short getId() {
    return id;
  }

  public AccountType getName() {
    return name;
  }
}
