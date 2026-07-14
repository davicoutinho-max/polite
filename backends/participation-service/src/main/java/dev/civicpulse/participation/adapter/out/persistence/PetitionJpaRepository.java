package dev.civicpulse.participation.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

interface PetitionJpaRepository extends JpaRepository<PetitionJpaEntity, UUID> {

  @Query("select p from PetitionJpaEntity p order by p.id")
  List<PetitionJpaEntity> findAllOrdered(Pageable pageable);
}
