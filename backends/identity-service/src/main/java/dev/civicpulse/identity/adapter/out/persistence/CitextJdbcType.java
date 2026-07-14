package dev.civicpulse.identity.adapter.out.persistence;

import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

/**
 * Maps a Postgres {@code citext} column. Binding/extraction is identical to plain VARCHAR (the
 * driver accepts {@code setString}/{@code getString} for citext transparently) — the only
 * difference is {@link #getJdbcTypeCode()}, which must report {@link SqlTypes#OTHER} to match
 * what the Postgres JDBC driver reports for citext columns, so Hibernate's schema validator
 * (ddl-auto: validate) doesn't flag a false VARCHAR-vs-OTHER mismatch on startup.
 */
final class CitextJdbcType extends VarcharJdbcType {

  static final CitextJdbcType INSTANCE = new CitextJdbcType();

  @Override
  public int getJdbcTypeCode() {
    return SqlTypes.OTHER;
  }
}
