package dev.civicpulse.feedcontent.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PostAgendaDetailsJpaRepository extends JpaRepository<PostAgendaDetailsJpaEntity, UUID> {}
