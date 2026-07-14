package dev.civicpulse.identity.adapter.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SessionJpaRepository extends JpaRepository<SessionJpaEntity, UUID> {

  Optional<SessionJpaEntity> findByRefreshTokenHash(String refreshTokenHash);

  @Query(
      "select s from SessionJpaEntity s where s.accountId = :accountId and s.revokedAt is null and s.expiresAt > :now")
  List<SessionJpaEntity> findActiveByAccountId(@Param("accountId") UUID accountId, @Param("now") Instant now);
}
