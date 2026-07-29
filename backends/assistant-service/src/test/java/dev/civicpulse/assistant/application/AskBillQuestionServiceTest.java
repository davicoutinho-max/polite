package dev.civicpulse.assistant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.assistant.application.port.out.GeminiGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AskBillQuestionServiceTest {

  @Mock private GeminiGateway geminiGateway;

  @Test
  void asksGeminiWithBillContextAndReturnsAnswer() {
    AskBillQuestionService service = new AskBillQuestionService(geminiGateway);
    when(geminiGateway.generateAnswer(any(), any())).thenReturn("This bill proposes...");

    String answer = service.ask("PL 1234/2024", "Regulates something.", "What does this bill do?");

    assertThat(answer).isEqualTo("This bill proposes...");
    verify(geminiGateway)
        .generateAnswer(
            contains("Only answer questions about the bill"),
            contains("PL 1234/2024") //
        );
  }
}
