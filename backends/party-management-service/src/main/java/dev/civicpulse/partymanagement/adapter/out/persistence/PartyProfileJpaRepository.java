package dev.civicpulse.partymanagement.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PartyProfileJpaRepository extends JpaRepository<PartyProfileJpaEntity, UUID> {}
