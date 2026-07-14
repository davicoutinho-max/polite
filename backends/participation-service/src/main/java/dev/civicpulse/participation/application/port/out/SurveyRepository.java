package dev.civicpulse.participation.application.port.out;

import dev.civicpulse.participation.domain.model.Survey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SurveyRepository {

  Survey save(Survey survey);

  Optional<Survey> findById(UUID id);

  List<Survey> findAll(int page, int pageSize);
}
