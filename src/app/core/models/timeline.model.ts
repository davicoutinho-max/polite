/** A milestone in the long-form career timeline (2018 → 2025) on a profile. */
export interface CareerMilestone {
  readonly year: number;
  readonly title: string;
  readonly detail?: string;
}

/** Category of a feed event on the activity timeline — mirrors activity-feed-service's
 * timeline_event_type_options; only types with a real upstream event producer exist. */
export type TimelineEventType =
  | 'vote'
  | 'project'
  | 'pec'
  | 'cpi'
  | 'status_change'
  | 'committee'
  | 'video'
  | 'post'
  | 'party_change'
  | 'campaign';

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
