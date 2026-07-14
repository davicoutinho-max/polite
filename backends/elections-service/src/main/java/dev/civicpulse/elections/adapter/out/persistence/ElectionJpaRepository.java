package dev.civicpulse.elections.adapter.out.persistence;

import dev.civicpulse.elections.domain.model.ElectionScope;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ElectionJpaRepository extends JpaRepository<ElectionJpaEntity, UUID> {

  @Query("select e from ElectionJpaEntity e order by e.electionDate asc")
  List<ElectionJpaEntity> findAllOrderByElectionDateAsc(Pageable pageable);

  @Query("select e from ElectionJpaEntity e where e.scope = :scope order by e.electionDate asc")
  List<ElectionJpaEntity> findByScope(@Param("scope") ElectionScope scope, Pageable pageable);

  @Query("select e from ElectionJpaEntity e where e.electionDate >= :from order by e.electionDate asc")
  List<ElectionJpaEntity> findUpcoming(@Param("from") LocalDate from, Pageable pageable);
}
