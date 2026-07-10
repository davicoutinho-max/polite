/**
 * Visual severity shared by every status chip / tag in the app.
 * Keeps the color language consistent across posts, bills and profiles.
 */
export type TagSeverity =
  | 'primary'
  | 'secondary'
  | 'success'
  | 'warning'
  | 'danger'
  | 'info'
  | 'neutral';

export interface StatusTag {
  readonly label: string;
  readonly severity: TagSeverity;
  /** Optional material-symbol icon rendered before the label. */
  readonly icon?: string;
  readonly uppercase?: boolean;
}
