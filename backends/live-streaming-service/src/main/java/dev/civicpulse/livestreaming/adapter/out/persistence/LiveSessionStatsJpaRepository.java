package dev.civicpulse.livestreaming.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface LiveSessionStatsJpaRepository extends JpaRepository<LiveSessionStatsJpaEntity, UUID> {}
