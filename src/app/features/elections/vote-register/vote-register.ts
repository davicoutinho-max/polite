import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AutoComplete, AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { ElectionService } from '../../../core/services/election.service';
import { SessionService } from '../../../core/services/session.service';
import { ElectionResult, PersonalVote } from '../../../core/models';
import { UiCard } from '../../../shared/ui/ui-card/ui-card';
import { UiIcon } from '../../../shared/ui/ui-icon/ui-icon';
import { UiEmpty } from '../../../shared/ui/ui-empty/ui-empty';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';
import { TranslateService } from '../../../core/services/translate.service';

type Choice = ElectionResult | string;

interface OfficeBallot {
  readonly office: string;
  readonly candidatePool: ElectionResult[];
}

/** Personal, unofficial vote registration — a citizen searches for who they picked in each race
 * of an election and saves their own memory of it. Deliberately distinct in tone/layout from the
 * rest of the platform (dark "ballot" cards, one race at a time, a green confirm action) while
 * still built from the same shared UI primitives — see PersonalVote's backend javadoc for why
 * this can never be, or resemble, the real secret ballot. */
@Component({
  selector: 'app-vote-register-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, FormsModule, AutoComplete, UiCard, UiIcon, UiEmpty, TranslatePipe],
  templateUrl: './vote-register.html',
  styleUrl: './vote-register.scss',
})
export class VoteRegisterPage {
  private readonly electionService = inject(ElectionService);
  private readonly session = inject(SessionService);
  protected readonly translate = inject(TranslateService);

  readonly id = input.required<string>();

  protected readonly isAuthenticated = this.session.isAuthenticated;

  protected readonly election = computed(() => this.electionService.byId(this.id()));
  private readonly results = computed(() => this.electionService.resultsOf(this.id()));
  private readonly candidates = computed(() => this.electionService.candidatesOf(this.id()));
  protected readonly myVotes = computed(() => this.electionService.myVotesOf(this.id()));

  protected readonly ballots = computed<OfficeBallot[]>(() => {
    const byOffice = new Map<string, ElectionResult[]>();
    const addTo = (office: string, result: ElectionResult) => {
      const list = byOffice.get(office);
      if (list) {
        list.push(result);
      } else {
        byOffice.set(office, [result]);
      }
    };

    for (const result of this.results()) {
      addTo(result.office, result);
    }
    if (byOffice.size === 0) {
      for (const candidate of this.candidates()) {
        const office = candidate.office || this.translate.t('label.candidate', 'Candidate');
        addTo(office, {
          id: candidate.id,
          office,
          candidateName: candidate.name,
          partyAcronym: candidate.partyAcronym,
          votes: 0,
          rank: 0,
          elected: false,
          politicianAccountId: candidate.id,
        });
      }
    }
    return [...byOffice.entries()].map(([office, candidatePool]) => ({ office, candidatePool }));
  });

  private readonly filteredByOffice = signal<Map<string, ElectionResult[]>>(new Map());
  private readonly selectionByOffice = signal<Map<string, Choice>>(new Map());
  private readonly editingOffices = signal<ReadonlySet<string>>(new Set());

  constructor() {
    effect(() => {
      const electionId = this.id();
      this.electionService.loadResults(electionId).subscribe();
      if (this.isAuthenticated()) {
        this.electionService.loadMyVotes(electionId).subscribe();
      }
    });
  }

  protected votedFor(office: string): PersonalVote | undefined {
    return this.myVotes().find((v) => v.office === office);
  }

  protected isEditing(office: string): boolean {
    return this.editingOffices().has(office) || !this.votedFor(office);
  }

  protected startEditing(office: string): void {
    this.editingOffices.update((set) => new Set(set).add(office));
  }

  protected suggestionsFor(office: string): ElectionResult[] {
    return this.filteredByOffice().get(office) ?? [];
  }

  protected selectionFor(office: string): Choice | null {
    return this.selectionByOffice().get(office) ?? null;
  }

  protected setSelection(office: string, value: Choice): void {
    this.selectionByOffice.update((map) => new Map(map).set(office, value));
  }

  protected search(office: string, event: AutoCompleteCompleteEvent, pool: ElectionResult[]): void {
    const query = event.query.trim().toLowerCase();
    const matches = query ? pool.filter((c) => c.candidateName.toLowerCase().includes(query)) : pool.slice(0, 15);
    this.filteredByOffice.update((map) => new Map(map).set(office, matches.slice(0, 15)));
  }

  protected displayName(choice: Choice | null): string {
    if (!choice) return '';
    return typeof choice === 'string' ? choice : choice.candidateName;
  }

  protected canConfirm(office: string): boolean {
    const choice = this.selectionFor(office);
    return typeof choice === 'string' ? choice.trim().length > 0 : choice !== null;
  }

  protected confirm(office: string): void {
    const choice = this.selectionFor(office);
    if (!choice) return;
    const candidateName = typeof choice === 'string' ? choice.trim() : choice.candidateName;
    if (!candidateName) return;
    const partyAcronym = typeof choice === 'string' ? null : choice.partyAcronym || null;
    const politicianAccountId = typeof choice === 'string' ? null : choice.politicianAccountId;

    this.electionService.registerMyVote(this.id(), office, candidateName, partyAcronym, politicianAccountId).subscribe(() => {
      this.editingOffices.update((set) => {
        const next = new Set(set);
        next.delete(office);
        return next;
      });
    });
  }
}
