import { ChangeDetectionStrategy, Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { LegislativeBillDetail, LegislativeSource, LegislativeTimelineEntry, LegislativeVotingRecord } from '../../../core/models';
import { AiAssistantService } from '../../../core/services/ai-assistant.service';
import { LegislativeOpenDataService } from '../../../core/services/legislative-open-data.service';
import { TranslateService } from '../../../core/services/translate.service';
import { AskAi, AskAiPromptOption } from '../../../shared/ai/ask-ai/ask-ai';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';
import { PageHeader } from '../../../shared/ui/page-header/page-header';
import { UiCard } from '../../../shared/ui/ui-card/ui-card';
import { UiEmpty } from '../../../shared/ui/ui-empty/ui-empty';
import { UiIcon } from '../../../shared/ui/ui-icon/ui-icon';
import { UiTag } from '../../../shared/ui/ui-tag/ui-tag';
import { UiSkeleton } from '../../../shared/ui/ui-skeleton/ui-skeleton';

/** Detail view for a single real bill (Câmara/Senado open-data) — reached from a bill card on the
 * Bills page. Route is `/bills/:source/:id` (not just `:id`) because the two chambers' ids aren't
 * from the same namespace — see LegislativeOpenDataService. Fetches its own full detail rather
 * than reusing the list card's already-loaded summary, since the list endpoint doesn't carry the
 * richer fields this page shows (full ementa, author, current status, voting record). */
@Component({
  selector: 'app-bill-detail-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, PageHeader, UiCard, UiTag, UiIcon, UiEmpty, UiSkeleton, AskAi, TranslatePipe],
  templateUrl: './bill-detail.html',
  styleUrl: './bill-detail.scss',
})
export class BillDetailPage implements OnInit {
  private readonly legislativeOpenData = inject(LegislativeOpenDataService);
  private readonly aiAssistant = inject(AiAssistantService);
  protected readonly translate = inject(TranslateService);

  readonly source = input.required<LegislativeSource>();
  readonly id = input.required<string>();

  protected readonly loading = signal(true);
  protected readonly notFound = signal(false);
  protected readonly bill = signal<LegislativeBillDetail | null>(null);

  protected readonly askAiTopicLabel = computed(() => {
    const b = this.bill();
    return b ? `${b.identification} — ${b.fullSummary || b.summary}` : '';
  });
  protected readonly askAiFn = computed(() => (question: string): Observable<string> => {
    const b = this.bill()!;
    return this.aiAssistant.askAboutBill(b.identification, b.fullSummary || b.summary, question);
  });
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

  protected readonly votingLoading = signal(true);
  protected readonly votingRecords = signal<LegislativeVotingRecord[]>([]);

  protected readonly timelineLoading = signal(true);
  protected readonly timeline = signal<LegislativeTimelineEntry[]>([]);

  ngOnInit(): void {
    this.legislativeOpenData.getBillDetail(this.source(), this.id()).subscribe({
      next: (detail) => {
        this.loading.set(false);
        this.bill.set(detail);
        this.notFound.set(detail === null);
      },
      error: () => {
        this.loading.set(false);
        this.notFound.set(true);
      },
    });
    this.legislativeOpenData.getVotingRecords(this.source(), this.id()).subscribe({
      next: (records) => {
        this.votingLoading.set(false);
        this.votingRecords.set(records);
      },
      error: () => {
        this.votingLoading.set(false);
        this.votingRecords.set([]);
      },
    });
    this.legislativeOpenData.getTimeline(this.source(), this.id()).subscribe({
      next: (entries) => {
        this.timelineLoading.set(false);
        this.timeline.set(entries);
      },
      error: () => {
        this.timelineLoading.set(false);
        this.timeline.set([]);
      },
    });
  }

  protected tallyTotal(record: LegislativeVotingRecord): number {
    const t = record.tally;
    return t ? t.yes + t.no + t.abstain + t.absent : 0;
  }
}
