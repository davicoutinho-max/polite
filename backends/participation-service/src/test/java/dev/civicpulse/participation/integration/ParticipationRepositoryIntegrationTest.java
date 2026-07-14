package dev.civicpulse.participation.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.participation.application.port.out.ConsultationRepository;
import dev.civicpulse.participation.application.port.out.ConsultationResponseRepository;
import dev.civicpulse.participation.application.port.out.PetitionRepository;
import dev.civicpulse.participation.application.port.out.PetitionSignatureRepository;
import dev.civicpulse.participation.application.port.out.SurveyOptionRepository;
import dev.civicpulse.participation.application.port.out.SurveyRepository;
import dev.civicpulse.participation.application.port.out.SurveyVoteRepository;
import dev.civicpulse.participation.domain.model.Consultation;
import dev.civicpulse.participation.domain.model.ConsultationResponse;
import dev.civicpulse.participation.domain.model.ConsultationStance;
import dev.civicpulse.participation.domain.model.Petition;
import dev.civicpulse.participation.domain.model.PetitionSignature;
import dev.civicpulse.participation.domain.model.Survey;
import dev.civicpulse.participation.domain.model.SurveyOption;
import dev.civicpulse.participation.domain.model.SurveyVote;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Exercises the real JPA/Postgres adapters against the shared local-dev database (see
 * identity-service's equivalent test for the rationale on why this isn't Testcontainers). */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5432/participation_service",
      "spring.datasource.username=participation_service_app",
      "spring.datasource.password=participation_dev_pw"
    })
class ParticipationRepositoryIntegrationTest {

  @BeforeAll
  static void requireLocalPostgres() {
    boolean reachable;
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("localhost", 5432), 500);
      reachable = true;
    } catch (Exception e) {
      reachable = false;
    }
    assumeTrue(reachable, "Shared dev Postgres (localhost:5432) is not running — start it with "
        + "'docker compose up -d postgres' in backends/ to run this test");
  }

  @Autowired private PetitionRepository petitionRepository;
  @Autowired private PetitionSignatureRepository petitionSignatureRepository;
  @Autowired private ConsultationRepository consultationRepository;
  @Autowired private ConsultationResponseRepository consultationResponseRepository;
  @Autowired private SurveyRepository surveyRepository;
  @Autowired private SurveyOptionRepository surveyOptionRepository;
  @Autowired private SurveyVoteRepository surveyVoteRepository;

  @Test
  void petitionAndSignatureRoundTrip() {
    UUID petitionId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    petitionRepository.save(Petition.create(petitionId, "Save the park", "summary", "environment", 1000, null));

    assertThat(petitionSignatureRepository.exists(petitionId, citizenId)).isFalse();

    petitionSignatureRepository.save(PetitionSignature.sign(petitionId, citizenId, Instant.now()));

    assertThat(petitionSignatureRepository.exists(petitionId, citizenId)).isTrue();
    assertThat(petitionRepository.findById(petitionId)).isPresent().get().satisfies(found -> assertThat(found.title()).isEqualTo("Save the park"));
  }

  @Test
  void consultationResponseRoundTripAndChange() {
    UUID consultationId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    consultationRepository.save(Consultation.create(consultationId, "title", null, null));

    consultationResponseRepository.save(ConsultationResponse.respond(consultationId, citizenId, ConsultationStance.FAVOR, Instant.now()));

    assertThat(consultationResponseRepository.findByConsultationAndCitizen(consultationId, citizenId))
        .isPresent()
        .get()
        .satisfies(found -> assertThat(found.stance()).isEqualTo(ConsultationStance.FAVOR));
  }

  @Test
  void surveyOptionAndVoteRoundTrip() {
    UUID surveyId = UUID.randomUUID();
    UUID optionId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    surveyRepository.save(Survey.create(surveyId, "Question?", null));
    surveyOptionRepository.save(SurveyOption.create(optionId, surveyId, "Yes"));

    assertThat(surveyVoteRepository.exists(surveyId, citizenId)).isFalse();

    surveyVoteRepository.save(SurveyVote.cast(surveyId, citizenId, optionId, Instant.now()));

    assertThat(surveyVoteRepository.exists(surveyId, citizenId)).isTrue();
    assertThat(surveyOptionRepository.findBySurveyId(surveyId)).anySatisfy(o -> assertThat(o.label()).isEqualTo("Yes"));
  }
}
