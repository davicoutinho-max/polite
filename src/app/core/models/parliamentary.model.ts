import { StatusTag } from './tag.model';

/** A legislative production item: project, law, PEC, CPI, etc. */
export interface LegislativeItem {
  readonly id: string;
  readonly reference: string;
  readonly title: string;
  readonly summary: string;
  readonly status: StatusTag;
  readonly date: string;
}

export interface CommitteeMembership {
  readonly name: string;
  readonly role: string;
  readonly kind: 'committee' | 'front' | 'cpi';
}

export interface VoteRecord {
  readonly id: string;
  readonly matter: string;
  readonly date: string;
  readonly vote: 'yes' | 'no' | 'abstain' | 'absent';
}

/** Aggregated attendance stats. */
export interface AttendanceStats {
  readonly present: number;
  readonly absent: number;
  readonly presenceRate: number;
}

/** Everything shown under the "Parliamentary activity" tab. */
export interface ParliamentaryActivity {
  readonly projects: LegislativeItem[];
  readonly approvedLaws: LegislativeItem[];
  readonly rejectedLaws: LegislativeItem[];
  readonly pecs: LegislativeItem[];
  readonly cpis: LegislativeItem[];
  readonly committees: CommitteeMembership[];
  readonly votes: VoteRecord[];
  readonly attendance: AttendanceStats;
  readonly speeches: number;
  readonly interviews: number;
  readonly trips: number;
}
