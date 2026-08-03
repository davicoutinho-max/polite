import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { LegislativeBillSummary, LegislativeTimelineEntry } from '../../../core/models';
import { AiAssistantService } from '../../../core/services/ai-assistant.service';
import { LegislativeOpenDataService } from '../../../core/services/legislative-open-data.service';
import { TranslateService } from '../../../core/services/translate.service';
import { AskAi, AskAiPromptOption } from '../../ai/ask-ai/ask-ai';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { UiButton } from '../../ui/ui-button/ui-button';
import { UiCard } from '../../ui/ui-card/ui-card';
import { UiDialog } from '../../ui/ui-dialog/ui-dialog';
import { UiIcon } from '../../ui/ui-icon/ui-icon';
import { UiTag } from '../../ui/ui-tag/ui-tag';
import { UiSkeleton } from '../../ui/ui-skeleton/ui-skeleton';

/** One real bill (Câmara/Senado open-data), with its own "View history" (real tramitação, shown
 * as a timeline) and "Ask AI" (real Gemini call, see shared/legislative/ask-ai) modal. Shared
 * between the standalone Bills page and the petition card's "Related bills" dialog so this
 * non-trivial UI/state isn't duplicated in two places. */
@Component({
  selector: 'app-bill-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, UiCard, UiTag, UiButton, UiIcon, UiDialog, UiSkeleton, AskAi, TranslatePipe],
  templateUrl: './bill-card.html',
  styleUrl: './bill-card.scss',
})
export class BillCard {
  private readonly legislativeOpenData = inject(LegislativeOpenDataService);
  private readonly aiAssistant = inject(AiAssistantService);
  private readonly translate = inject(TranslateService);

  readonly bill = input.required<LegislativeBillSummary>();

  protected readonly askAiTopicLabel = computed(() => `${this.bill().identification} — ${this.bill().summary}`);
  protected readonly askAiFn = computed(
    () => (question: string): Observable<string> => this.aiAssistant.askAboutBill(this.bill().identification, this.bill().summary, question),
  );
  protected readonly askAiPrompts: AskAiPromptOption[] = [
    { question: 'Summarize this bill in one or two sentences.', label: this.translate.t('button.ask-ai-summarize', 'Summarize'), icon: 'summarize' },
    {
      question: 'Explain this bill in simple, plain terms a non-lawyer would understand.',
      label: this.translate.t('button.ask-ai-explain-simply', 'Explain simply'),
      icon: 'lightbulb',
    },
    {
      question: 'What would change in practice if this bill is approved?',
      label: this.translate.t('button.ask-ai-impact', 'What changes if approved?'),
      icon: 'trending_up',
    },
  ];

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
    this.legislativeOpenData.getTimeline(this.bill().source, this.bill().id).subscribe({
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
}
