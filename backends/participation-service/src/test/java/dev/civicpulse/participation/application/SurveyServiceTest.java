package dev.civicpulse.participation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.participation.application.port.out.EventPublisher;
import dev.civicpulse.participation.application.port.out.SurveyOptionRepository;
import dev.civicpulse.participation.application.port.out.SurveyRepository;
import dev.civicpulse.participation.application.port.out.SurveyVoteRepository;
import dev.civicpulse.participation.domain.exception.AlreadyVotedException;
import dev.civicpulse.participation.domain.exception.SurveyNotFoundException;
import dev.civicpulse.participation.domain.exception.SurveyOptionNotFoundException;
import dev.civicpulse.participation.domain.model.Survey;
import dev.civicpulse.participation.domain.model.SurveyOption;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private SurveyRepository surveyRepository;
  @Mock private SurveyOptionRepository surveyOptionRepository;
  @Mock private SurveyVoteRepository surveyVoteRepository;
  @Mock private EventPublisher eventPublisher;

  private SurveyService service;

  @BeforeEach
  void setUp() {
    service = new SurveyService(surveyRepository, surveyOptionRepository, surveyVoteRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void createSavesSurveyAndOptions() {
    when(surveyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Survey survey = service.create("Question?", "ctx", List.of("Yes", "No"));

    assertThat(survey.question()).isEqualTo("Question?");
    verify(surveyOptionRepository, times(2)).save(any());
  }

  @Test
  void voteThrowsWhenAlreadyVoted() {
    UUID surveyId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    when(surveyVoteRepository.exists(surveyId, citizenId)).thenReturn(true);

    assertThatThrownBy(() -> service.vote(surveyId, citizenId, UUID.randomUUID())).isInstanceOf(AlreadyVotedException.class);
  }

  @Test
  void voteThrowsWhenSurveyMissing() {
    UUID surveyId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    when(surveyVoteRepository.exists(surveyId, citizenId)).thenReturn(false);
    when(surveyRepository.findById(surveyId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.vote(surveyId, citizenId, UUID.randomUUID())).isInstanceOf(SurveyNotFoundException.class);
  }

  @Test
  void voteThrowsWhenOptionMissing() {
    UUID surveyId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    UUID optionId = UUID.randomUUID();
    when(surveyVoteRepository.exists(surveyId, citizenId)).thenReturn(false);
    when(surveyRepository.findById(surveyId)).thenReturn(Optional.of(Survey.create(surveyId, "q", null)));
    when(surveyOptionRepository.findById(optionId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.vote(surveyId, citizenId, optionId)).isInstanceOf(SurveyOptionNotFoundException.class);
  }

  @Test
  void voteIncrementsOptionAndPublishesEvent() {
    UUID surveyId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    UUID optionId = UUID.randomUUID();
    SurveyOption option = SurveyOption.create(optionId, surveyId, "Yes");
    when(surveyVoteRepository.exists(surveyId, citizenId)).thenReturn(false);
    when(surveyRepository.findById(surveyId)).thenReturn(Optional.of(Survey.create(surveyId, "q", null)));
    when(surveyOptionRepository.findById(optionId)).thenReturn(Optional.of(option));

    service.vote(surveyId, citizenId, optionId);

    assertThat(option.votesCount()).isEqualTo(1);
    verify(surveyVoteRepository).save(any());
    verify(eventPublisher).publish(any());
  }
}
