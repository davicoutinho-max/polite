package dev.civicpulse.participation.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SurveyVoteJpaRepository extends JpaRepository<SurveyVoteJpaEntity, SurveyVoteId> {

  boolean existsBySurveyIdAndCitizenAccountId(UUID surveyId, UUID citizenAccountId);
}
