export interface TransparencyMetric {
  readonly id: string;
  readonly icon: string;
  readonly label: string;
  readonly value: string;
  readonly caption: string;
  readonly period: string;
}

export interface ExpenseLine {
  readonly category: string;
  readonly amount: number;
  /** 0–100 share of the total, for the bar. */
  readonly share: number;
}

export interface TransparencyReport {
  readonly metrics: TransparencyMetric[];
  readonly expenses: ExpenseLine[];
  readonly totalExpense: string;
  readonly lastUpdate: string;
}

/** Real public-money categories a Brazilian federal politician is accountable for — mirrors
 * legislative-service's AccountabilityCategory enum. */
export type AccountabilityCategory =
  | 'office_budget'
  | 'parliamentary_quota'
  | 'parliamentary_amendments'
  | 'travel_allowance'
  | 'advertising';

export const ACCOUNTABILITY_CATEGORIES: readonly { readonly value: AccountabilityCategory; readonly label: string; readonly icon: string }[] = [
  { value: 'office_budget', label: 'Office budget', icon: 'business_center' },
  { value: 'parliamentary_quota', label: 'Parliamentary activity quota (CEAP)', icon: 'receipt_long' },
  { value: 'parliamentary_amendments', label: 'Parliamentary amendments', icon: 'account_balance' },
  { value: 'travel_allowance', label: 'Travel allowance', icon: 'flight' },
  { value: 'advertising', label: 'Institutional advertising', icon: 'campaign' },
];

/** One AI-reviewed accountability submission — see legislative-service's
 * AccountabilityDisclosure javadoc for the full workflow. Every submission is kept (not just the
 * latest), so a category's "current" status is simply its most recent submission. */
export interface AccountabilityDisclosure {
  readonly id: string;
  readonly category: AccountabilityCategory;
  readonly declaredAmountCents: number;
  readonly documentUrl: string;
  readonly status: 'approved' | 'rejected';
  readonly extractedAmountCents: number | null;
  readonly aiFeedback: string;
  readonly submittedAt: string;
}
