import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, finalize, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Election, ElectionCandidateSummary, ElectionResult, ElectionScope, PersonalVote } from '../models';

interface ElectionResponseDto {
  readonly id: string;
  readonly title: string;
  readonly scope: string;
  readonly electionDate: string;
  readonly location: string | null;
  readonly description: string | null;
}

interface CandidateResponseDto {
  readonly accountId: string;
  readonly name: string;
  readonly avatarUrl: string | null;
  readonly office: string | null;
  readonly partyId: string | null;
  readonly partyAcronym: string | null;
}

interface ResultResponseDto {
  readonly id: string;
  readonly office: string;
  readonly candidateName: string;
  readonly partyAcronym: string | null;
  readonly votes: number;
  readonly rank: number;
  readonly elected: boolean;
  readonly politicianAccountId: string | null;
}

interface PersonalVoteResponseDto {
  readonly id: string;
  readonly office: string;
  readonly candidateName: string;
  readonly candidatePartyAcronym: string | null;
  readonly politicianAccountId: string | null;
  readonly castAt: string;
}

function capitalizeScope(scope: string): ElectionScope {
  return (scope.charAt(0).toUpperCase() + scope.slice(1)) as ElectionScope;
}

function formatDate(iso: string): string {
  const date = new Date(`${iso}T00:00:00`);
  return Number.isNaN(date.getTime()) ? iso : date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

@Injectable({ providedIn: 'root' })
export class ElectionService {
  private readonly http = inject(HttpClient);
  private readonly apiBase = `${environment.apiBaseUrl}/api/elections`;

  private readonly _elections = signal<Election[]>([]);
  readonly elections = this._elections.asReadonly();

  private readonly _candidatesByElection = signal<Map<string, ElectionCandidateSummary[]>>(new Map());

  /** Unlike candidates (eagerly preloaded for every election in {@link reload}), results are
   * loaded lazily per-election on demand — a single state election alone can carry 300+ rows
   * (every Deputado Estadual candidate, not just winners), so preloading them for every election
   * up front the way candidates are would be wasteful. */
  private readonly _resultsByElection = signal<Map<string, ElectionResult[]>>(new Map());
  private readonly _resultsLoading = signal<Map<string, boolean>>(new Map());

  readonly upcomingCount = computed(() => this._elections().filter((e) => this.isUpcoming(e)).length);
  readonly totalCandidates = computed(() => {
    const ids = new Set<string>();
    for (const candidates of this._candidatesByElection().values()) {
      for (const c of candidates) {
        ids.add(c.id);
      }
    }
    return ids.size;
  });
  readonly nextDate = computed(() => {
    const upcoming = this._elections()
      .filter((e) => this.isUpcoming(e))
      .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
    return upcoming[0]?.date ?? '—';
  });

  /** Most recent year first — matches how a citizen naturally thinks about "the 2024 elections",
   * "the 2022 elections", etc. rather than a flat scope-only filter. */
  readonly years = computed(() => [...new Set(this._elections().map((e) => e.year))].sort((a, b) => b - a));

  constructor() {
    this.reload().subscribe();
  }

  reload(page = 0, pageSize = 100): Observable<Election[]> {
    return this.http.get<ElectionResponseDto[]>(`${this.apiBase}/elections`, { params: { page, pageSize } }).pipe(
      map((list) =>
        list.map(
          (e): Election => ({
            id: e.id,
            title: e.title,
            scope: capitalizeScope(e.scope),
            date: formatDate(e.electionDate),
            year: Number(e.electionDate.slice(0, 4)),
            location: e.location,
            description: e.description ?? '',
          }),
        ),
      ),
      tap((elections) => this._elections.set(elections)),
      switchMap((elections) => (elections.length ? forkJoin(elections.map((e) => this.loadCandidates(e.id))) : of([]))),
      map(() => this._elections()),
    );
  }

  loadCandidates(electionId: string): Observable<ElectionCandidateSummary[]> {
    return this.http.get<CandidateResponseDto[]>(`${this.apiBase}/elections/${electionId}/candidacies`).pipe(
      map((list) =>
        list.map(
          (c): ElectionCandidateSummary => ({
            id: c.accountId,
            name: c.name,
            avatarUrl: c.avatarUrl ?? '',
            office: c.office ?? '',
            partyId: c.partyId ?? '',
            partyAcronym: c.partyAcronym ?? '',
          }),
        ),
      ),
      tap((candidates) => this._candidatesByElection.update((map) => new Map(map).set(electionId, candidates))),
    );
  }

  candidatesOf(electionId: string): ElectionCandidateSummary[] {
    return this._candidatesByElection().get(electionId) ?? [];
  }

  loadResults(electionId: string): Observable<ElectionResult[]> {
    this._resultsLoading.update((map) => new Map(map).set(electionId, true));
    return this.http.get<ResultResponseDto[]>(`${this.apiBase}/elections/${electionId}/results`).pipe(
      map((list) =>
        list.map(
          (r): ElectionResult => ({
            id: r.id,
            office: r.office,
            candidateName: r.candidateName,
            partyAcronym: r.partyAcronym ?? '',
            votes: r.votes,
            rank: r.rank,
            elected: r.elected,
            politicianAccountId: r.politicianAccountId,
          }),
        ),
      ),
      tap((results) => this._resultsByElection.update((map) => new Map(map).set(electionId, results))),
      finalize(() => this._resultsLoading.update((map) => new Map(map).set(electionId, false))),
    );
  }

  resultsLoadingOf(electionId: string): boolean {
    return this._resultsLoading().get(electionId) ?? false;
  }

  resultsOf(electionId: string): ElectionResult[] {
    return this._resultsByElection().get(electionId) ?? [];
  }

  private readonly _myVotesByElection = signal<Map<string, PersonalVote[]>>(new Map());

  loadMyVotes(electionId: string): Observable<PersonalVote[]> {
    return this.http.get<PersonalVoteResponseDto[]>(`${this.apiBase}/elections/${electionId}/my-votes`).pipe(
      map((list) =>
        list.map(
          (v): PersonalVote => ({
            id: v.id,
            office: v.office,
            candidateName: v.candidateName,
            candidatePartyAcronym: v.candidatePartyAcronym ?? '',
            politicianAccountId: v.politicianAccountId,
            castAt: v.castAt,
          }),
        ),
      ),
      tap((votes) => this._myVotesByElection.update((map) => new Map(map).set(electionId, votes))),
    );
  }

  myVotesOf(electionId: string): PersonalVote[] {
    return this._myVotesByElection().get(electionId) ?? [];
  }

  /** Personal/unofficial only — see PersonalVote's javadoc. Idempotent per {@code office}:
   * registering again for the same office updates the citizen's existing pick. */
  registerMyVote(
    electionId: string,
    office: string,
    candidateName: string,
    candidatePartyAcronym: string | null,
    politicianAccountId: string | null,
  ): Observable<PersonalVote> {
    return this.http
      .post<PersonalVoteResponseDto>(`${this.apiBase}/elections/${electionId}/my-votes`, {
        office,
        candidateName,
        candidatePartyAcronym,
        politicianAccountId,
      })
      .pipe(
        map(
          (v): PersonalVote => ({
            id: v.id,
            office: v.office,
            candidateName: v.candidateName,
            candidatePartyAcronym: v.candidatePartyAcronym ?? '',
            politicianAccountId: v.politicianAccountId,
            castAt: v.castAt,
          }),
        ),
        tap((vote) =>
          this._myVotesByElection.update((map) => {
            const next = new Map(map);
            const existing = (next.get(electionId) ?? []).filter((v) => v.office !== office);
            next.set(electionId, [...existing, vote]);
            return next;
          }),
        ),
      );
  }

  /** Lets a party nominate one of its politicians as a pre-candidate for an upcoming election —
   * backed by the same `POST /elections/{id}/candidacies` endpoint used by the government-sync
   * pipeline (see ElectionController's javadoc: writes here are trusted to whatever gateway-level
   * policy fronts this service, same as every other admin-style endpoint in this system). */
  nominateCandidate(electionId: string, politicianAccountId: string): Observable<void> {
    return this.http
      .post<void>(`${this.apiBase}/elections/${electionId}/candidacies`, { politicianAccountId })
      .pipe(switchMap(() => this.loadCandidates(electionId).pipe(map(() => undefined))));
  }

  byId(electionId: string): Election | undefined {
    return this._elections().find((e) => e.id === electionId);
  }

  isUpcoming(election: Election): boolean {
    return new Date(election.date).getTime() >= Date.now();
  }
}
