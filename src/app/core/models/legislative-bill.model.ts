/** Real bills (Projetos de Lei) from the federal legislature's own open-data APIs — Câmara dos
 * Deputados and Senado Federal — not internal CivicPulse data. See LegislativeOpenDataService. */
export type LegislativeSource = 'camara' | 'senado';

export interface LegislativeBillSummary {
  readonly source: LegislativeSource;
  readonly id: string;
  readonly identification: string;
  readonly typeLabel: string;
  readonly summary: string;
  readonly presentedDate: string | null;
  readonly officialUrl: string;
}

export interface LegislativeTimelineEntry {
  readonly date: string;
  readonly description: string;
  readonly location: string | null;
}

/** Everything the bill detail page shows beyond the card summary — sourced from each API's own
 * per-bill detail endpoint (Câmara's `/proposicoes/{id}`, Senado's `/materia/{codigo}`), not
 * derivable from the list endpoint. Any field the source API doesn't have for a given bill stays
 * `null` and the detail page shows nothing for it, rather than a fabricated value. */
export interface LegislativeBillDetail extends LegislativeBillSummary {
  readonly fullSummary: string | null;
  readonly author: string | null;
  readonly currentStatusDescription: string | null;
  readonly currentStatusDate: string | null;
  readonly currentStatusLocation: string | null;
  readonly fullTextUrl: string | null;
  readonly keywords: string | null;
}

/** A single roll-call/plenary vote on a bill. `tally` is only present for Câmara bills that had
 * an actual nominal (roll-call) vote — most Câmara votes and effectively all Senado ones in this
 * open dataset are symbolic (voz/acclamation) and never get individual votes recorded at all, so
 * `tally` staying `null` is the normal case, not a loading failure. */
export interface LegislativeVotingRecord {
  readonly id: string;
  readonly date: string;
  readonly description: string;
  readonly approved: boolean | null;
  readonly location: string | null;
  readonly tally: { readonly yes: number; readonly no: number; readonly abstain: number; readonly absent: number } | null;
}

export interface LegislativeBillType {
  readonly code: string;
  readonly label: string;
}

/** The bill types citizens actually care about — the dozens of purely-procedural REQ/MSC/INC
 * subtypes both APIs also expose are omitted since they're not "laws in progress" in the sense
 * this page is for. */
export const LEGISLATIVE_BILL_TYPES: readonly LegislativeBillType[] = [
  { code: 'PL', label: 'Bill (PL)' },
  { code: 'PEC', label: 'Constitutional Amendment (PEC)' },
  { code: 'PLP', label: 'Complementary Law (PLP)' },
  { code: 'MPV', label: 'Provisional Measure (MPV)' },
  { code: 'PDL', label: 'Legislative Decree (PDL)' },
];
