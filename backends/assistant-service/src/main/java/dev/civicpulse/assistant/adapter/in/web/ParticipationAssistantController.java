package dev.civicpulse.assistant.adapter.in.web;

import dev.civicpulse.assistant.adapter.in.web.dto.AskParticipationQuestionRequest;
import dev.civicpulse.assistant.adapter.in.web.dto.AskParticipationQuestionResponse;
import dev.civicpulse.assistant.application.port.in.AskParticipationQuestionUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public, visitor-accessible — "Ask AI" needs no account, same as the participation pages it's
 * attached to. Abuse is guarded by request-size limits (see AskParticipationQuestionRequest) and
 * the model's own system instruction (see AskParticipationQuestionService), not by
 * authentication. */
@RestController
@RequestMapping("/assistant/participation")
public class ParticipationAssistantController {

  private final AskParticipationQuestionUseCase askParticipationQuestionUseCase;

  public ParticipationAssistantController(AskParticipationQuestionUseCase askParticipationQuestionUseCase) {
    this.askParticipationQuestionUseCase = askParticipationQuestionUseCase;
  }

  @PostMapping("/ask")
  public AskParticipationQuestionResponse ask(@Valid @RequestBody AskParticipationQuestionRequest request) {
    String answer = askParticipationQuestionUseCase.ask(request.itemType(), request.title(), request.description(), request.question());
    return new AskParticipationQuestionResponse(answer);
  }
}
