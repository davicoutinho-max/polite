package dev.civicpulse.directory.adapter.out.persistence;

import dev.civicpulse.directory.domain.model.PartySpectrum;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PartyJpaRepository extends JpaRepository<PartyJpaEntity, UUID> {

  @Query("select p from PartyJpaEntity p where (:spectrum is null or p.spectrum = :spectrum) order by p.name asc")
  List<PartyJpaEntity> search(@Param("spectrum") PartySpectrum spectrum, Pageable pageable);
}
