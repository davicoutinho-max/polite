export type AlertCategory = 'project' | 'party' | 'pec' | 'cpi' | 'vote' | 'campaign';

export interface Alert {
  readonly id: string;
  readonly category: AlertCategory;
  readonly icon: string;
  readonly title: string;
  readonly message: string;
  readonly timeLabel: string;
  /** Optional deep link opened when the notification is clicked. */
  readonly link?: string;
  read: boolean;
}
