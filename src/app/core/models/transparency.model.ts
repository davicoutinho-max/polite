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

/** One AI-reviewed accountability submission for a single month/year, attached to one of the
 * compensation/CEAP/office-budget line items shown on the transparency tab (`category` matches
 * one of those items' own key — see legislative-service's accountability_category_options).
 * Every submission is kept (not just the latest), so a rejected attempt's AI feedback stays
 * visible and the full history for a given item+period is browsable. */
export interface AccountabilityDisclosure {
  readonly id: string;
  readonly category: string;
  /** 1–12. */
  readonly periodMonth: number;
  readonly periodYear: number;
  readonly declaredAmountCents: number;
  readonly documentUrl: string;
  readonly status: 'approved' | 'rejected';
  readonly extractedAmountCents: number | null;
  readonly aiFeedback: string;
  /** Optional free-text context the politician added — not seen by the AI reviewer, purely for
   * their own public record. */
  readonly notes: string | null;
  readonly submittedAt: string;
}
