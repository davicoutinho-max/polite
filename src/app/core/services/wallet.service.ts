import { computed, inject, Injectable, signal } from '@angular/core';
import { DigitalCard, FiliationStatus, FiliationStep, MembershipFee } from '../models';
import { AlertsService } from './alerts.service';

/** Ordered stages of the official affiliation flow. */
const FILIATION_STEPS: FiliationStep[] = [
  {
    status: 'requested',
    label: 'Request sent',
    description: 'Pre-registration and documents submitted through the app.',
    icon: 'send',
  },
  {
    status: 'under-review',
    label: 'Party review',
    description: 'The party is validating your documents and contribution.',
    icon: 'fact_check',
  },
  {
    status: 'party-approved',
    label: 'Party approved',
    description: 'Your local directory approved the affiliation request.',
    icon: 'verified_user',
  },
  {
    status: 'electoral-justice',
    label: 'Sent to Electoral Justice',
    description: 'The party filed your affiliation with the Electoral Justice.',
    icon: 'gavel',
  },
  {
    status: 'affiliated',
    label: 'Officially affiliated',
    description: 'Registration confirmed. Your digital membership card is active.',
    icon: 'badge',
  },
];

const ORDER: FiliationStatus[] = [
  'not-started',
  'requested',
  'under-review',
  'party-approved',
  'electoral-justice',
  'affiliated',
];

@Injectable({ providedIn: 'root' })
export class WalletService {
  private readonly alerts = inject(AlertsService);

  readonly steps = FILIATION_STEPS;

  private readonly _status = signal<FiliationStatus>('party-approved');
  readonly status = this._status.asReadonly();

  /** Index into the step list; -1 while not started. */
  readonly currentStepIndex = computed(() => ORDER.indexOf(this._status()) - 1);
  readonly isAffiliated = computed(() => this._status() === 'affiliated');

  readonly card = signal<DigitalCard>({
    memberId: 'PP-2026-004821',
    holderName: 'Alex Morgan',
    party: 'Progressive Party',
    partyAcronym: 'PP',
    since: 'Jul 6, 2026',
    qrPayload: 'https://civicpulse.gov/card/PP-2026-004821',
  }).asReadonly();

  private readonly _fees = signal<MembershipFee[]>([
    { id: 'f1', reference: 'Jul/2026', amount: 35, dueDate: 'Jul 10, 2026', status: 'pending' },
    { id: 'f2', reference: 'Jun/2026', amount: 35, dueDate: 'Jun 10, 2026', status: 'paid', paidAt: 'Jun 8, 2026' },
    { id: 'f3', reference: 'May/2026', amount: 35, dueDate: 'May 10, 2026', status: 'paid', paidAt: 'May 9, 2026' },
    { id: 'f4', reference: 'Apr/2026', amount: 35, dueDate: 'Apr 10, 2026', status: 'paid', paidAt: 'Apr 7, 2026' },
  ]);
  readonly fees = this._fees.asReadonly();
  readonly pendingFee = computed(() => this._fees().find((f) => f.status !== 'paid'));

  /** Advance the affiliation state machine one stage. */
  advance(): void {
    const idx = ORDER.indexOf(this._status());
    if (idx < ORDER.length - 1) {
      this._status.set(ORDER[idx + 1]);
    }
  }

  requestFiliation(): void {
    if (this._status() === 'not-started') {
      this._status.set('requested');
      this.alerts.push({
        category: 'party',
        icon: 'how_to_reg',
        title: 'Affiliation request sent',
        message:
          'Your request was submitted to the party. Official confirmation depends on the party and the Electoral Justice.',
        timeLabel: 'Just now',
        link: '/wallet',
      });
    }
  }

  reset(): void {
    this._status.set('not-started');
  }

  payFee(id: string): void {
    this._fees.update((fees) =>
      fees.map((fee) =>
        fee.id === id ? { ...fee, status: 'paid' as const, paidAt: 'Just now' } : fee,
      ),
    );
  }
}
