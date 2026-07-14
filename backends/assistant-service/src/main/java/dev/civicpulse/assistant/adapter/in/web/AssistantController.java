package dev.civicpulse.assistant.adapter.in.web;

import dev.civicpulse.assistant.adapter.in.web.dto.AssistantTopicResponse;
import dev.civicpulse.assistant.adapter.in.web.dto.CreateTopicRequest;
import dev.civicpulse.assistant.adapter.in.web.dto.WriteAnswerRequest;
import dev.civicpulse.assistant.application.port.in.AssistantUseCase;
import dev.civicpulse.assistant.domain.model.AssistantPromptKind;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assistant/topics")
public class AssistantController {

  private final AssistantUseCase assistantUseCase;

  public AssistantController(AssistantUseCase assistantUseCase) {
    this.assistantUseCase = assistantUseCase;
  }

  @GetMapping
  public List<AssistantTopicResponse> list() {
    return assistantUseCase.listTopics().stream().map(AssistantTopicResponse::from).toList();
  }

  @GetMapping("/{id}")
  public AssistantTopicResponse getTopic(@PathVariable UUID id) {
    return AssistantTopicResponse.from(assistantUseCase.getTopic(id));
  }

  @PostMapping
  public ResponseEntity<AssistantTopicResponse> createTopic(@Valid @RequestBody CreateTopicRequest request) {
    AssistantTopicResponse body = AssistantTopicResponse.from(assistantUseCase.createTopic(request.reference(), request.title(), request.legislativeItemId()));
    return ResponseEntity.created(URI.create("/assistant/topics/" + body.id())).body(body);
  }

  @PostMapping("/{id}/answers")
  public AssistantTopicResponse writeAnswer(@PathVariable UUID id, @Valid @RequestBody WriteAnswerRequest request) {
    return AssistantTopicResponse.from(assistantUseCase.writeAnswer(id, AssistantPromptKind.fromCode(request.promptKind()), request.answerText()));
  }
}
