package dev.civicpulse.participation.application;

import dev.civicpulse.participation.application.port.in.GetSurveyUseCase;
import dev.civicpulse.participation.application.port.in.ManageSurveyUseCase;
import dev.civicpulse.participation.application.port.out.EventPublisher;
import dev.civicpulse.participation.application.port.out.SurveyOptionRepository;
import dev.civicpulse.participation.application.port.out.SurveyRepository;
import dev.civicpulse.participation.application.port.out.SurveyVoteRepository;
import dev.civicpulse.participation.domain.event.SurveyVoteCast;
import dev.civicpulse.participation.domain.exception.AlreadyVotedException;
import dev.civicpulse.participation.domain.exception.SurveyNotFoundException;
import dev.civicpulse.participation.domain.exception.SurveyOptionNotFoundException;
import dev.civicpulse.participation.domain.model.Survey;
import dev.civicpulse.participation.domain.model.SurveyOption;
import dev.civicpulse.participation.domain.model.SurveyVote;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyService implements ManageSurveyUseCase, GetSurveyUseCase {

  private final SurveyRepository surveyRepository;
  private final SurveyOptionRepository surveyOptionRepository;
  private final SurveyVoteRepository surveyVoteRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public SurveyService(
      SurveyRepository surveyRepository,
      SurveyOptionRepository surveyOptionRepository,
      SurveyVoteRepository surveyVoteRepository,
      EventPublisher eventPublisher,
      Clock clock) {
    this.surveyRepository = surveyRepository;
    this.surveyOptionRepository = surveyOptionRepository;
    this.surveyVoteRepository = surveyVoteRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Survey create(String question, String context, List<String> optionLabels) {
    Survey survey = surveyRepository.save(Survey.create(UUID.randomUUID(), question, context));
    for (String label : optionLabels) {
      surveyOptionRepository.save(SurveyOption.create(UUID.randomUUID(), survey.id(), label));
    }
    return survey;
  }

  @Override
  @Transactional
  public void vote(UUID surveyId, UUID citizenAccountId, UUID optionId) {
    if (surveyVoteRepository.exists(surveyId, citizenAccountId)) {
      throw new AlreadyVotedException();
    }
    if (surveyRepository.findById(surveyId).isEmpty()) {
      throw new SurveyNotFoundException(surveyId);
    }
    SurveyOption option = surveyOptionRepository.findById(optionId).orElseThrow(() -> new SurveyOptionNotFoundException(optionId));

    Instant now = clock.instant();
    surveyVoteRepository.save(SurveyVote.cast(surveyId, citizenAccountId, optionId, now));
    option.incrementVotes();
    surveyOptionRepository.save(option);

    eventPublisher.publish(new SurveyVoteCast(surveyId, citizenAccountId, optionId, now));
  }

  @Override
  @Transactional(readOnly = true)
  public Survey getById(UUID id) {
    return surveyRepository.findById(id).orElseThrow(() -> new SurveyNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Survey> list(int page, int pageSize) {
    return surveyRepository.findAll(page, pageSize);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SurveyOption> listOptions(UUID surveyId) {
    return surveyOptionRepository.findBySurveyId(surveyId);
  }
}
