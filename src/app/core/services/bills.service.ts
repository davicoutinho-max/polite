import { Injectable, signal } from '@angular/core';
import { Bill } from '../models';

@Injectable({ providedIn: 'root' })
export class BillsService {
  /** Compact bills shown in the feed sidebar. */
  readonly relevantBills = signal<Bill[]>([
    {
      id: 'b1',
      reference: 'S.Res. 105',
      title: 'Urban Housing Development Act',
      sponsor: 'Sponsored by Sen. Williams',
      status: { label: 'Vetoed', severity: 'danger', uppercase: true },
    },
    {
      id: 'b2',
      reference: 'H.R. 201',
      title: 'Public Transit Expansion',
      sponsor: 'Sponsored by Rep. Chen',
      status: { label: 'Passed', severity: 'success', uppercase: true },
    },
  ]).asReadonly();

  /** Rich legislation cards shown on a politician profile. */
  readonly politicianBills = signal<Bill[]>([
    {
      id: 'pb1',
      reference: 'Bill H.R. 452',
      title: 'Clean Water Infrastructure Act',
      summary:
        'A comprehensive proposal to modernize municipal water treatment facilities and reduce lead pipe usage in urban districts over the next decade.',
      kind: { label: 'Bill H.R. 452', severity: 'neutral' },
      status: { label: 'Committee', severity: 'warning' },
      progress: 45,
      progressLabel: 'Legislative Progress',
      progressColor: 'secondary',
      metaIcon: 'group',
      metaLabel: '12 Co-sponsors',
      linkLabel: 'Read Full Text',
    },
    {
      id: 'pb2',
      reference: 'Local Initiative',
      title: 'Downtown Small Business Grant Program',
      summary:
        'Establishing a $5M fund to support local merchants recovering from economic downturns, focusing on minority-owned enterprises.',
      kind: { label: 'Local Initiative', severity: 'neutral' },
      status: { label: 'Active', severity: 'secondary' },
      progress: 75,
      progressLabel: 'Funding Distributed',
      progressColor: 'tertiary',
      metaIcon: 'payments',
      metaLabel: '$3.75M Allocated',
      linkLabel: 'View Ledger',
    },
  ]).asReadonly();
}
