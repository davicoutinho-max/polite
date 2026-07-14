package dev.civicpulse.feedcontent.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PostMetricsJpaRepository extends JpaRepository<PostMetricsJpaEntity, UUID> {}
