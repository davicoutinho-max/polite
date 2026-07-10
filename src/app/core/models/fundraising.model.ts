/**
 * Fundraising is intentionally scoped to social causes and party initiatives
 * OUTSIDE the electoral context. Electoral campaign fundraising has its own
 * legal regime (official systems, accountability) and is not handled here.
 */
export type FundraiserCategory = 'social' | 'party' | 'humanitarian';

export interface FundraiserCategoryMeta {
  readonly category: FundraiserCategory;
  readonly label: string;
  readonly icon: string;
}

export interface Fundraiser {
  readonly id: string;
  readonly title: string;
  readonly description: string;
  readonly organizer: string;
  readonly category: FundraiserCategory;
  readonly goal: number;
  raised: number;
  supporters: number;
  readonly deadline: string;
  /** Whether an itemized ledger is publicly available (transparency). */
  readonly ledgerPublic: boolean;
}

export interface NewFundraiser {
  readonly title: string;
  readonly description: string;
  readonly category: FundraiserCategory;
  readonly goal: number;
  readonly deadline: string;
}
