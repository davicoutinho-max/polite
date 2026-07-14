package dev.civicpulse.participation.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface SurveyJpaRepository extends JpaRepository<SurveyJpaEntity, UUID> {

  @Query("select s from SurveyJpaEntity s order by s.id")
  List<SurveyJpaEntity> findAllOrdered(Pageable pageable);
}
