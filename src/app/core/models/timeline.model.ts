/** A milestone in the long-form career timeline (2018 → 2025) on a profile. */
export interface CareerMilestone {
  readonly year: number;
  readonly title: string;
  readonly detail?: string;
}

/** Category of a feed event on the activity timeline. */
export type TimelineEventType =
  | 'vote'
  | 'project'
  | 'committee'
  | 'video'
  | 'event'
  | 'honor'
  | 'party-change'
  | 'campaign'
  | 'accounts';

/** A single entry in the activity timeline feed. */
export interface TimelineEvent {
  readonly id: string;
  readonly type: TimelineEventType;
  readonly title: string;
  readonly detail?: string;
  readonly timeLabel: string;
  readonly group: string;
  readonly actor: string;
}
