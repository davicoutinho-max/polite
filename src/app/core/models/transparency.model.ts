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
