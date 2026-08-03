package dev.civicpulse.directory.adapter.out.persistence;

import dev.civicpulse.directory.domain.model.PartySpectrum;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PartyJpaRepository extends JpaRepository<PartyJpaEntity, UUID> {

  // :q is cast explicitly — left untyped, a null :q makes Postgres infer the '%'||:q||'%'
  // concatenation as bytea instead of text (confirmed: this 500s the plain, no-search listing
  // every visitor hits, since :q is always null there), even though the identical pattern with
  // more sibling string/enum params in PoliticianJpaRepository.search happens not to trigger it.
  @Query(
      "select p from PartyJpaEntity p "
          + "where (:spectrum is null or p.spectrum = :spectrum) "
          + "and (:q is null or lower(p.name) like lower(concat('%', cast(:q as string), '%'))) "
          + "order by p.name asc")
  List<PartyJpaEntity> search(@Param("spectrum") PartySpectrum spectrum, @Param("q") String q, Pageable pageable);
}
