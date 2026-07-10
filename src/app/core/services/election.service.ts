import { computed, inject, Injectable, signal } from '@angular/core';
import { Election, ElectionCandidate, PoliticianSummary } from '../models';
import { DirectoryService } from './directory.service';

@Injectable({ providedIn: 'root' })
export class ElectionService {
  private readonly directory = inject(DirectoryService);

  private readonly _elections = signal<Election[]>([
    {
      id: 'e1',
      title: 'General Election 2026',
      scope: 'Nacional',
      date: 'Oct 4, 2026',
      description: 'Federal Deputies and Senators up for election nationwide.',
    },
    {
      id: 'e2',
      title: 'São Paulo Gubernatorial By-election',
      scope: 'Estadual',
      date: 'Nov 15, 2026',
      description: 'Special election to fill the vacant governorship of São Paulo.',
    },
    {
      id: 'e3',
      title: 'Campinas Municipal Runoff',
      scope: 'Municipal',
      date: 'Dec 6, 2026',
      description: 'Second-round vote for mayor between the top two candidates.',
    },
    {
      id: 'e4',
      title: 'General Election 2022',
      scope: 'Nacional',
      date: 'Oct 2, 2022',
      description: 'Previous federal election cycle.',
    },
  ]);
  readonly elections = this._elections.asReadonly();

  private readonly _candidacies = signal<ElectionCandidate[]>([
    { electionId: 'e1', politicianId: 'pol-1' },
    { electionId: 'e1', politicianId: 'pol-2' },
    { electionId: 'e1', politicianId: 'pol-3' },
    { electionId: 'e2', politicianId: 'pol-5' },
    { electionId: 'e2', politicianId: 'pol-6' },
    { electionId: 'e3', politicianId: 'pol-8' },
    { electionId: 'e4', politicianId: 'pol-1' },
  ]);

  readonly upcomingCount = computed(
    () => this._elections().filter((e) => new Date(e.date).getTime() >= Date.now()).length,
  );
  readonly totalCandidates = computed(() => new Set(this._candidacies().map((c) => c.politicianId)).size);
  readonly nextDate = computed(() => {
    const upcoming = this._elections()
      .filter((e) => new Date(e.date).getTime() >= Date.now())
      .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
    return upcoming[0]?.date ?? '—';
  });

  candidatesOf(electionId: string): PoliticianSummary[] {
    const ids = new Set(
      this._candidacies().filter((c) => c.electionId === electionId).map((c) => c.politicianId),
    );
    return this.directory.politicians().filter((p) => ids.has(p.id));
  }

  byId(electionId: string): Election | undefined {
    return this._elections().find((e) => e.id === electionId);
  }

  isUpcoming(election: Election): boolean {
    return new Date(election.date).getTime() >= Date.now();
  }
}
