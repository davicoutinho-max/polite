package dev.civicpulse.assistant.adapter.in.web;

import dev.civicpulse.assistant.adapter.in.web.dto.AskBillQuestionRequest;
import dev.civicpulse.assistant.adapter.in.web.dto.AskBillQuestionResponse;
import dev.civicpulse.assistant.application.port.in.AskBillQuestionUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public, visitor-accessible — "Ask AI" needs no account, same as the bill pages it's attached
 * to. Abuse is guarded by request-size limits (see AskBillQuestionRequest) and the model's own
 * system instruction (see AskBillQuestionService), not by authentication. */
@RestController
@RequestMapping("/assistant/bills")
public class BillAssistantController {

  private final AskBillQuestionUseCase askBillQuestionUseCase;

  public BillAssistantController(AskBillQuestionUseCase askBillQuestionUseCase) {
    this.askBillQuestionUseCase = askBillQuestionUseCase;
  }

  @PostMapping("/ask")
  public AskBillQuestionResponse ask(@Valid @RequestBody AskBillQuestionRequest request) {
    String answer = askBillQuestionUseCase.ask(request.billIdentification(), request.billSummary(), request.question());
    return new AskBillQuestionResponse(answer);
  }
}
