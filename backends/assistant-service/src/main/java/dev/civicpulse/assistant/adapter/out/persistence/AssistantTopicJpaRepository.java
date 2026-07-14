package dev.civicpulse.assistant.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AssistantTopicJpaRepository extends JpaRepository<AssistantTopicJpaEntity, UUID> {}
