/** Government level an election is held at. */
export type ElectionScope = 'Nacional' | 'Estadual' | 'Municipal';

/** A scheduled or past election, visible to everyone including visitors. */
export interface Election {
  readonly id: string;
  readonly title: string;
  readonly scope: ElectionScope;
  /** Display label, e.g. "Oct 4, 2026". */
  readonly date: string;
  readonly description: string;
}

/** Links a politician (by directory id) to an election as a candidate. */
export interface ElectionCandidate {
  readonly electionId: string;
  readonly politicianId: string;
}
