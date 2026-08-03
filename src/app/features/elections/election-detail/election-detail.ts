import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AutoComplete, AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { ElectionService } from '../../../core/services/election.service';
import { DirectoryService } from '../../../core/services/directory.service';
import { SessionService } from '../../../core/services/session.service';
import { ElectionResult, PoliticianSummary, TagSeverity } from '../../../core/models';
import { PageHeader } from '../../../shared/ui/page-header/page-header';
import { UiButton } from '../../../shared/ui/ui-button/ui-button';
import { UiCard } from '../../../shared/ui/ui-card/ui-card';
import { UiTag } from '../../../shared/ui/ui-tag/ui-tag';
import { UiAvatar } from '../../../shared/ui/ui-avatar/ui-avatar';
import { UiIcon } from '../../../shared/ui/ui-icon/ui-icon';
import { UiEmpty } from '../../../shared/ui/ui-empty/ui-empty';
import { UiSkeleton } from '../../../shared/ui/ui-skeleton/ui-skeleton';
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
  imports: [RouterLink, DecimalPipe, FormsModule, AutoComplete, PageHeader, UiButton, UiCard, UiTag, UiAvatar, UiIcon, UiEmpty, UiSkeleton, TranslatePipe],
  templateUrl: './election-detail.html',
  styleUrl: './election-detail.scss',
})
export class ElectionDetailPage {
  private readonly electionService = inject(ElectionService);
  private readonly directory = inject(DirectoryService);
  private readonly session = inject(SessionService);
  private readonly translate = inject(TranslateService);

  readonly id = input.required<string>();

  private readonly expandedOffices = signal<ReadonlySet<string>>(new Set());

  protected readonly election = computed(() => this.electionService.byId(this.id()));
  protected readonly candidates = computed(() => this.electionService.candidatesOf(this.id()));
  protected readonly results = computed(() => this.electionService.resultsOf(this.id()));
  protected readonly resultsLoading = computed(() => this.electionService.resultsLoadingOf(this.id()));

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

  /** "MDB · No. 15" — the party's real registered ballot number, resolved from the already-loaded
   * directory cache (each candidacy only carries the party acronym/id, not a per-candidate
   * number — this platform doesn't track individual TSE candidate numbers, only who's linked to
   * which party). */
  protected candidateParty(partyId: string): string | null {
    const party = this.directory.parties().find((p) => p.id === partyId);
    if (!party) {
      return null;
    }
    return `${party.acronym} · ${this.translate.t('label.ballot-number-abbr', 'No.')} ${party.number}`;
  }

  // ---- Pre-candidate nomination (party accounts only, upcoming elections only) ----
  protected readonly canNominate = computed(() => this.session.can('party-admin') && this.isUpcoming());
  protected readonly nominationSuggestions = signal<PoliticianSummary[]>([]);
  protected readonly nominationSelection = signal<PoliticianSummary | null>(null);
  protected readonly nominating = signal(false);
  protected readonly nominationError = signal('');

  protected searchPoliticians(event: AutoCompleteCompleteEvent): void {
    const query = event.query.trim().toLowerCase();
    const alreadyIn = new Set(this.candidates().map((c) => c.id));
    const pool = this.directory.politicians().filter((p) => !alreadyIn.has(p.id));
    const matches = query ? pool.filter((p) => p.name.toLowerCase().includes(query)) : pool.slice(0, 15);
    this.nominationSuggestions.set(matches.slice(0, 15));
  }

  protected nominate(): void {
    const politician = this.nominationSelection();
    const electionId = this.id();
    if (!politician) {
      return;
    }
    this.nominating.set(true);
    this.nominationError.set('');
    this.electionService.nominateCandidate(electionId, politician.id).subscribe({
      next: () => {
        this.nominating.set(false);
        this.nominationSelection.set(null);
      },
      error: () => {
        this.nominating.set(false);
        this.nominationError.set(this.translate.t('error.nominate-candidate-failed', 'Could not add this pre-candidate — please try again.'));
      },
    });
  }
}
