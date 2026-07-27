/** Government level an election is held at. */
export type ElectionScope = 'Nacional' | 'Estadual' | 'Municipal';

/** A scheduled or past election, visible to everyone including visitors. */
export interface Election {
  readonly id: string;
  readonly title: string;
  readonly scope: ElectionScope;
  /** Display label, e.g. "Oct 4, 2026". */
  readonly date: string;
  readonly year: number;
  /** UF for ESTADUAL elections, município name for MUNICIPAL ones, null for NACIONAL — see
   * elections-service's Election.location javadoc for why this reuses that exact convention. */
  readonly location: string | null;
  readonly description: string;
}

/** Links a politician (by directory id) to an election as a candidate. */
export interface ElectionCandidate {
  readonly electionId: string;
  readonly politicianId: string;
}

/** Compact candidate view for an election's candidacy list — elections-service resolves these
 * fields itself, so this is intentionally narrower than the full directory PoliticianSummary. */
export interface ElectionCandidateSummary {
  readonly id: string;
  readonly name: string;
  readonly avatarUrl: string;
  readonly office: string;
  readonly partyAcronym: string;
}

/** One candidate's real tally in one race (office) of an election, sourced from TSE — unlike
 * {@link ElectionCandidateSummary}, this covers every candidate who ran (not just the ones who
 * also have a platform account) and carries the real vote count and rank within their race.
 * {@code politicianAccountId} is null for candidates who never got a platform account (almost
 * everyone who didn't win). */
export interface ElectionResult {
  readonly id: string;
  readonly office: string;
  readonly candidateName: string;
  readonly partyAcronym: string;
  readonly votes: number;
  readonly rank: number;
  readonly elected: boolean;
  readonly politicianAccountId: string | null;
}

/** A citizen's own, personal record of who they picked for one office — see elections-service's
 * PersonalVote javadoc: this is never the official secret ballot, purely a self-reported memory
 * aid the citizen can review or change later. */
export interface PersonalVote {
  readonly id: string;
  readonly office: string;
  readonly candidateName: string;
  readonly candidatePartyAcronym: string;
  readonly politicianAccountId: string | null;
  readonly castAt: string;
}
