import { StatusTag } from './tag.model';

export interface Bill {
  readonly id: string;
  /** Reference code, e.g. "H.R. 452" or "S.Res. 105". */
  readonly reference: string;
  readonly title: string;
  readonly summary?: string;
  readonly sponsor?: string;
  /** Sponsoring politician's account id, for linking to their profile. */
  readonly politicianId?: string;
  readonly status: StatusTag;
  /** Optional badge such as "Local Initiative" or "Bill". */
  readonly kind?: StatusTag;
  /** 0–100 legislative / funding progress. */
  readonly progress?: number;
  readonly progressLabel?: string;
  readonly progressColor?: 'secondary' | 'tertiary';
  readonly metaIcon?: string;
  readonly metaLabel?: string;
  readonly linkLabel?: string;
}
