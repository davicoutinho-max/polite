import { ChangeDetectionStrategy, Component, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LegislativeBillSummary, LegislativeTimelineEntry } from '../../../core/models';
import { LegislativeOpenDataService } from '../../../core/services/legislative-open-data.service';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { UiButton } from '../../ui/ui-button/ui-button';
import { UiCard } from '../../ui/ui-card/ui-card';
import { UiDialog } from '../../ui/ui-dialog/ui-dialog';
import { UiIcon } from '../../ui/ui-icon/ui-icon';
import { UiTag } from '../../ui/ui-tag/ui-tag';

interface AskAiPrompt {
  readonly kind: 'summary' | 'plain' | 'impact';
  readonly label: string;
  readonly icon: string;
}

interface AskAiMessage {
  readonly id: string;
  readonly role: 'user' | 'assistant';
  readonly text: string;
}

const ASK_AI_PROMPTS: AskAiPrompt[] = [
  { kind: 'summary', label: 'Summarize', icon: 'summarize' },
  { kind: 'plain', label: 'Explain simply', icon: 'lightbulb' },
  { kind: 'impact', label: 'What changes if approved?', icon: 'trending_up' },
];

/** One real bill (Câmara/Senado open-data), with its own "View history" (real tramitação, shown
 * as a timeline) and "Ask AI" (templated — not LLM-backed, consistent with this platform having
 * no real model integration anywhere — see the removed assistant-service's own scope note) modal.
 * Shared between the standalone Bills page and the petition card's "Related bills" dialog so this
 * non-trivial UI/state isn't duplicated in two places. */
@Component({
  selector: 'app-bill-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, RouterLink, UiCard, UiTag, UiButton, UiIcon, UiDialog, TranslatePipe],
  templateUrl: './bill-card.html',
  styleUrl: './bill-card.scss',
})
export class BillCard {
  private readonly legislativeOpenData = inject(LegislativeOpenDataService);

  readonly bill = input.required<LegislativeBillSummary>();

  // ---- View history (real tramitação, shown as a timeline) ----
  protected readonly showHistory = signal(false);
  protected readonly historyLoading = signal(false);
  protected readonly timeline = signal<LegislativeTimelineEntry[] | null>(null);

  protected openHistory(): void {
    this.showHistory.set(true);
    if (this.timeline() !== null) {
      return;
    }
    this.historyLoading.set(true);
    this.legislativeOpenData.getTimeline(this.bill()).subscribe({
      next: (entries) => {
        this.historyLoading.set(false);
        this.timeline.set(entries);
      },
      error: () => {
        this.historyLoading.set(false);
        this.timeline.set([]);
      },
    });
  }

  // ---- Ask AI (templated Q&A grounded in this bill's own official data, not a real model) ----
  protected readonly showAskAi = signal(false);
  protected readonly askAiPrompts = ASK_AI_PROMPTS;
  protected readonly askAiMessages = signal<AskAiMessage[]>([]);
  protected readonly askAiThinking = signal(false);
  protected readonly askAiQuestion = signal('');

  protected openAskAi(): void {
    this.showAskAi.set(true);
  }

  protected askPrompt(kind: AskAiPrompt['kind'], label: string): void {
    this.ask(label, () => this.answerForPrompt(kind));
  }

  protected askFreeText(): void {
    const question = this.askAiQuestion().trim();
    if (!question) {
      return;
    }
    this.askAiQuestion.set('');
    this.ask(question, () => this.answerForQuestion(question));
  }

  protected clearAskAi(): void {
    this.askAiMessages.set([]);
  }

  private ask(label: string, buildAnswer: () => string): void {
    const userMsg: AskAiMessage = { id: `u${Date.now()}`, role: 'user', text: label };
    this.askAiMessages.update((m) => [...m, userMsg]);
    this.askAiThinking.set(true);

    setTimeout(() => {
      const answerMsg: AskAiMessage = { id: `a${Date.now()}`, role: 'assistant', text: buildAnswer() };
      this.askAiMessages.update((m) => [...m, answerMsg]);
      this.askAiThinking.set(false);
    }, 600);
  }

  private answerForPrompt(kind: AskAiPrompt['kind']): string {
    const bill = this.bill();
    switch (kind) {
      case 'summary':
        return `${bill.identification} (${bill.typeLabel}): ${bill.summary}`;
      case 'plain':
        return `In plain terms — ${bill.summary}`;
      case 'impact':
        return `If approved, this would take effect as described in its own text: "${bill.summary}" — for the exact legal wording and current status, see the official page.`;
    }
  }

  private answerForQuestion(question: string): string {
    const bill = this.bill();
    const q = question.toLowerCase();

    if (/(autor|quem propôs|quem apresentou|author)/.test(q)) {
      return `Authorship isn't included in this summary — check the official page for the full author list: ${bill.officialUrl}`;
    }
    if (/(quando|data|date|apresenta)/.test(q)) {
      return bill.presentedDate ? `${bill.identification} was presented on ${bill.presentedDate}.` : 'The presentation date is not available.';
    }
    if (/(status|tramita|andamento|situação|progress)/.test(q)) {
      return `Use "View history" on this card for the real processing timeline, or see the official page: ${bill.officialUrl}`;
    }
    if (/(resum|sobre o que|o que é|about|summary)/.test(q)) {
      return `${bill.identification}: ${bill.summary}`;
    }
    return `Based on ${bill.identification}'s official ementa: "${bill.summary}"\n\nFor authorship, full text or current status, see the official page: ${bill.officialUrl}`;
  }
}
