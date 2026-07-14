package dev.civicpulse.identity.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface DocumentVerificationAttemptJpaRepository extends JpaRepository<DocumentVerificationAttemptJpaEntity, UUID> {}
