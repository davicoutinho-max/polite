import { ChangeDetectionStrategy, Component, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { Observable } from 'rxjs';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslateService } from '../../../core/services/translate.service';
import { UiButton } from '../../ui/ui-button/ui-button';
import { UiDialog } from '../../ui/ui-dialog/ui-dialog';
import { UiIcon } from '../../ui/ui-icon/ui-icon';

export interface AskAiPromptOption {
  readonly question: string;
  readonly label: string;
  readonly icon: string;
}

interface AskAiMessage {
  readonly id: string;
  readonly role: 'user' | 'assistant';
  readonly text: string;
}

/** Generic "Ask AI" chat dialog — a real, live Gemini call, grounded strictly in whatever single
 * topic the caller is asking about (a bill, a petition, a consultation, a survey…), with the
 * guardrails against off-topic/prompt-injection questions living entirely server-side in each
 * topic's own assistant-service use case (see AskBillQuestionService / AskParticipationQuestion-
 * Service). This component only owns the chat UI/state; `askFn` is the caller's own guardrailed
 * backend call for its topic, so this widget never needs to know which endpoint or system
 * instruction applies. */
@Component({
  selector: 'app-ask-ai',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, InputText, UiButton, UiDialog, UiIcon, TranslatePipe],
  templateUrl: './ask-ai.html',
  styleUrl: './ask-ai.scss',
})
export class AskAi {
  private readonly translate = inject(TranslateService);

  /** Short line shown at the top of the dialog identifying what's being discussed — e.g. a bill's
   * "PL 1234/2024 — ementa…" or a petition's title. */
  readonly topicLabel = input.required<string>();
  /** Performs the actual guardrailed AI call for this topic; supplied by the parent, which knows
   * which backend endpoint/system-instruction applies. */
  readonly askFn = input.required<(question: string) => Observable<string>>();
  /** Quick-start suggested questions, shown just above the composer — content-type-specific, so
   * callers supply their own (see BillCard/PetitionCard). */
  readonly prompts = input<readonly AskAiPromptOption[]>([]);

  protected readonly show = signal(false);
  protected readonly messages = signal<AskAiMessage[]>([]);
  protected readonly thinking = signal(false);
  protected readonly question = signal('');

  protected open(): void {
    this.show.set(true);
  }

  protected askPrompt(prompt: AskAiPromptOption): void {
    this.ask(prompt.label, prompt.question);
  }

  protected askFreeText(): void {
    const q = this.question().trim();
    if (!q) {
      return;
    }
    this.question.set('');
    this.ask(q, q);
  }

  protected clear(): void {
    this.messages.set([]);
  }

  private ask(displayText: string, question: string): void {
    const userMsg: AskAiMessage = { id: `u${Date.now()}`, role: 'user', text: displayText };
    this.messages.update((m) => [...m, userMsg]);
    this.thinking.set(true);

    this.askFn()(question).subscribe({
      next: (answer) => {
        this.thinking.set(false);
        this.messages.update((m) => [...m, { id: `a${Date.now()}`, role: 'assistant', text: answer }]);
      },
      error: () => {
        this.thinking.set(false);
        this.messages.update((m) => [
          ...m,
          {
            id: `a${Date.now()}`,
            role: 'assistant',
            text: this.translate.t('hint.ask-ai-unavailable', 'The AI assistant is temporarily unavailable — please try again shortly.'),
          },
        ]);
      },
    });
  }
}
