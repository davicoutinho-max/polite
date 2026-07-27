import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ElectionService } from '../../../core/services/election.service';
import { ElectionResult, TagSeverity } from '../../../core/models';
import { PageHeader } from '../../../shared/ui/page-header/page-header';
import { UiCard } from '../../../shared/ui/ui-card/ui-card';
import { UiTag } from '../../../shared/ui/ui-tag/ui-tag';
import { UiAvatar } from '../../../shared/ui/ui-avatar/ui-avatar';
import { UiIcon } from '../../../shared/ui/ui-icon/ui-icon';
import { UiEmpty } from '../../../shared/ui/ui-empty/ui-empty';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';
import { TranslateService } from '../../../core/services/translate.service';

const SCOPE_SEVERITY: Record<string, TagSeverity> = {
  Nacional: 'primary',
  Estadual: 'secondary',
  Municipal: 'info',
};

/** Proportional races (Deputado Estadual, Vereador) can list 300+ candidates — collapsed by
 * default to this many per office, with a "show all" toggle, rather than either hiding the long
 * tail entirely or always rendering hundreds of rows on first paint. */
const RESULTS_PREVIEW_LIMIT = 10;

/** Detail view for a single election — reached by clicking a card on the Elections page. */
@Component({
  selector: 'app-election-detail-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DecimalPipe, PageHeader, UiCard, UiTag, UiAvatar, UiIcon, UiEmpty, TranslatePipe],
  templateUrl: './election-detail.html',
  styleUrl: './election-detail.scss',
})
export class ElectionDetailPage {
  private readonly electionService = inject(ElectionService);
  private readonly translate = inject(TranslateService);

  readonly id = input.required<string>();

  private readonly expandedOffices = signal<ReadonlySet<string>>(new Set());

  protected readonly election = computed(() => this.electionService.byId(this.id()));
  protected readonly candidates = computed(() => this.electionService.candidatesOf(this.id()));
  protected readonly results = computed(() => this.electionService.resultsOf(this.id()));

  protected readonly resultsByOffice = computed(() => {
    const byOffice = new Map<string, ElectionResult[]>();
    for (const result of this.results()) {
      const list = byOffice.get(result.office) ?? [];
      list.push(result);
      byOffice.set(result.office, list);
    }
    for (const list of byOffice.values()) {
      list.sort((a, b) => a.rank - b.rank);
    }
    return [...byOffice.entries()].map(([office, candidates]) => ({ office, candidates }));
  });

  constructor() {
    effect(() => {
      const electionId = this.id();
      this.electionService.loadResults(electionId).subscribe();
    });
  }

  protected isExpanded(office: string): boolean {
    return this.expandedOffices().has(office);
  }

  protected previewOf(candidates: ElectionResult[], office: string): ElectionResult[] {
    return this.isExpanded(office) ? candidates : candidates.slice(0, RESULTS_PREVIEW_LIMIT);
  }

  protected hasMore(candidates: ElectionResult[]): boolean {
    return candidates.length > RESULTS_PREVIEW_LIMIT;
  }

  protected toggleExpanded(office: string): void {
    this.expandedOffices.update((set) => {
      const next = new Set(set);
      if (next.has(office)) {
        next.delete(office);
      } else {
        next.add(office);
      }
      return next;
    });
  }

  protected readonly isUpcoming = computed(() => {
    const e = this.election();
    return e ? this.electionService.isUpcoming(e) : false;
  });

  protected readonly statusLabel = computed(() =>
    this.isUpcoming()
      ? this.translate.t('label.upcoming', 'Upcoming')
      : this.translate.t('label.concluded', 'Concluded'),
  );

  protected scopeSeverity(scope: string): TagSeverity {
    return SCOPE_SEVERITY[scope] ?? 'neutral';
  }
}
