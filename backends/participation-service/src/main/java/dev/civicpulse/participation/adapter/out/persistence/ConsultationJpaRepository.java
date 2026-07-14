package dev.civicpulse.participation.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface ConsultationJpaRepository extends JpaRepository<ConsultationJpaEntity, UUID> {

  @Query("select c from ConsultationJpaEntity c order by c.id")
  List<ConsultationJpaEntity> findAllOrdered(Pageable pageable);
}
