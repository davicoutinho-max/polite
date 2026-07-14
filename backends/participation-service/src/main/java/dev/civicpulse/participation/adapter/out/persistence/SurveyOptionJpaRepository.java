package dev.civicpulse.participation.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SurveyOptionJpaRepository extends JpaRepository<SurveyOptionJpaEntity, UUID> {

  List<SurveyOptionJpaEntity> findBySurveyId(UUID surveyId);
}
