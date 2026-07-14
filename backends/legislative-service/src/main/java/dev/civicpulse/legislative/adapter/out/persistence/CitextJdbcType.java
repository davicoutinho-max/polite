package dev.civicpulse.legislative.adapter.out.persistence;

import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

/**
 * Maps a Postgres {@code citext} column (see identity-service's identical class for the full
 * rationale — Hibernate's schema validator otherwise flags a false VARCHAR-vs-OTHER mismatch).
 */
final class CitextJdbcType extends VarcharJdbcType {

  static final CitextJdbcType INSTANCE = new CitextJdbcType();

  @Override
  public int getJdbcTypeCode() {
    return SqlTypes.OTHER;
  }
}
