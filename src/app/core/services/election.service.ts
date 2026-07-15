import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Election, ElectionCandidateSummary, ElectionScope } from '../models';

interface ElectionResponseDto {
  readonly id: string;
  readonly title: string;
  readonly scope: string;
  readonly electionDate: string;
  readonly description: string | null;
}

interface CandidateResponseDto {
  readonly accountId: string;
  readonly name: string;
  readonly avatarUrl: string | null;
  readonly office: string | null;
  readonly partyAcronym: string | null;
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

  byId(electionId: string): Election | undefined {
    return this._elections().find((e) => e.id === electionId);
  }

  isUpcoming(election: Election): boolean {
    return new Date(election.date).getTime() >= Date.now();
  }
}
