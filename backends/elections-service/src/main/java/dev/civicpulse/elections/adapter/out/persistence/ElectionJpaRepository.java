package dev.civicpulse.elections.adapter.out.persistence;

import dev.civicpulse.elections.domain.model.ElectionScope;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

  // Plain "= :location" never matches when both sides are NULL (SQL semantics) — the explicit
  // "both null" branch is what makes this a correct idempotency lookup for NACIONAL elections too.
  @Query(
      "select e from ElectionJpaEntity e where e.scope = :scope and e.electionDate = :electionDate "
          + "and (e.location = :location or (e.location is null and :location is null))")
  Optional<ElectionJpaEntity> findByScopeAndElectionDateAndLocation(
      @Param("scope") ElectionScope scope, @Param("electionDate") LocalDate electionDate, @Param("location") String location);
}
