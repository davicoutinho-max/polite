import { HttpClient } from '@angular/common/http';
import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { DigitalCard, FiliationStatus, FiliationStep, MembershipFee, PaymentStatus } from '../models';
import { AlertsService } from './alerts.service';
import { DirectoryService } from './directory.service';
import { SessionService } from './session.service';

/** Ordered stages of the official affiliation flow. */
const FILIATION_STEPS: FiliationStep[] = [
  { status: 'requested', label: 'Request sent', description: 'Pre-registration and documents submitted through the app.', icon: 'send' },
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

const ORDER: FiliationStatus[] = ['not-started', 'requested', 'under-review', 'party-approved', 'electoral-justice', 'affiliated'];

const STATUS_MAP: Record<string, FiliationStatus> = {
  not_started: 'not-started',
  requested: 'requested',
  under_review: 'under-review',
  party_approved: 'party-approved',
  electoral_justice: 'electoral-justice',
  affiliated: 'affiliated',
};

const EMPTY_CARD: DigitalCard = { memberId: '', holderName: '', party: '', partyAcronym: '', since: '', qrPayload: '' };

interface AffiliationResponseDto {
  readonly id: string;
  readonly partyId: string;
  readonly status: string;
}

interface MembershipFeeResponseDto {
  readonly id: string;
  readonly referencePeriod: string;
  readonly amountCents: number;
  readonly dueDate: string;
  readonly status: string;
  readonly paidAt: string | null;
}

interface MembershipCardResponseDto {
  readonly memberNumber: string;
  readonly qrPayload: string;
  readonly issuedAt: string;
}

interface PaymentIntentResponseDto {
  readonly id: string;
  readonly status: string;
}

/**
 * Digital wallet: the citizen's affiliation lifecycle (membership-affiliation-service) and
 * monthly fee payments (membership-affiliation-service + payments-service's two-step
 * authorize-then-capture flow). "Simulate next step"/"Reset demo" map to the real
 * citizen-triggerable transitions where one exists (there is none between under-review and
 * party-approved — that step is the party admin's call, made elsewhere).
 */
@Injectable({ providedIn: 'root' })
export class WalletService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly directory = inject(DirectoryService);
  private readonly alerts = inject(AlertsService);
  private readonly membershipApiBase = `${environment.apiBaseUrl}/api/membership`;
  private readonly paymentsApiBase = `${environment.apiBaseUrl}/api/payments`;

  readonly steps = FILIATION_STEPS;

  private readonly _affiliationId = signal<string | null>(null);
  private readonly _status = signal<FiliationStatus>('not-started');
  readonly status = this._status.asReadonly();

  /** Index into the step list; -1 while not started. */
  readonly currentStepIndex = computed(() => ORDER.indexOf(this._status()) - 1);
  readonly isAffiliated = computed(() => this._status() === 'affiliated');

  private readonly _card = signal<DigitalCard | null>(null);
  readonly card = computed(() => this._card() ?? EMPTY_CARD);

  private readonly _fees = signal<MembershipFee[]>([]);
  readonly fees = this._fees.asReadonly();
  readonly pendingFee = computed(() => this._fees().find((f) => f.status !== 'paid'));

  constructor() {
    // Gated on session.ready() rather than fired immediately — see DirectoryService's
    // reloadFollowing for the full explanation of why an unconditional constructor-time call
    // here would silently and permanently desync from the real server-side affiliation state on
    // a hard refresh.
    effect(() => {
      if (this.session.ready()) {
        this.reload().subscribe();
      }
    });
  }

  reload(): Observable<void> {
    if (!this.session.isAuthenticated()) {
      return of(undefined);
    }
    const citizenId = this.session.account().id;
    return this.http.get<AffiliationResponseDto[]>(`${this.membershipApiBase}/affiliations`, { params: { citizenAccountId: citizenId } }).pipe(
      switchMap((list) => {
        const affiliation = list[0];
        if (!affiliation) {
          this._affiliationId.set(null);
          this._status.set('not-started');
          this._card.set(null);
          this._fees.set([]);
          return of(undefined);
        }
        this._affiliationId.set(affiliation.id);
        this._status.set(STATUS_MAP[affiliation.status] ?? 'not-started');
        return forkJoin({
          card: this.http
            .get<MembershipCardResponseDto>(`${this.membershipApiBase}/affiliations/${affiliation.id}/card`)
            .pipe(catchError(() => of(null))),
          fees: this.http.get<MembershipFeeResponseDto[]>(`${this.membershipApiBase}/affiliations/${affiliation.id}/fees`),
        }).pipe(
          map(({ card, fees }) => {
            const party = this.directory.parties().find((p) => p.id === affiliation.partyId);
            this._card.set(
              card
                ? {
                    memberId: card.memberNumber,
                    holderName: this.session.account().name,
                    party: party?.name ?? '',
                    partyAcronym: party?.acronym ?? '',
                    since: card.issuedAt.slice(0, 10),
                    qrPayload: card.qrPayload,
                  }
                : null,
            );
            this._fees.set(
              fees.map(
                (f): MembershipFee => ({
                  id: f.id,
                  reference: f.referencePeriod,
                  amount: f.amountCents / 100,
                  dueDate: f.dueDate,
                  status: f.status as PaymentStatus,
                  paidAt: f.paidAt?.slice(0, 10),
                }),
              ),
            );
          }),
        );
      }),
    );
  }

  requestFiliation(partyId: string, city: string): void {
    if (this._status() !== 'not-started') {
      return;
    }
    this.http.post<AffiliationResponseDto>(`${this.membershipApiBase}/affiliations`, { partyId, city }).subscribe({
      next: (affiliation) => {
        this._affiliationId.set(affiliation.id);
        this._status.set(STATUS_MAP[affiliation.status] ?? 'requested');
        this.alerts.push({
          category: 'party',
          icon: 'how_to_reg',
          title: 'Affiliation request sent',
          message: 'Your request was submitted to the party. Official confirmation depends on the party and the Electoral Justice.',
          timeLabel: 'Just now',
          link: '/wallet',
        });
      },
    });
  }

  /** Advances the one real citizen-triggerable transition available for the current status, if
   * any (there is none while under review — that step is the party admin's call). */
  advance(): void {
    const id = this._affiliationId();
    if (!id) {
      return;
    }
    if (this._status() === 'party-approved') {
      this.http.post<AffiliationResponseDto>(`${this.membershipApiBase}/affiliations/${id}/send-to-electoral-justice`, {}).subscribe({
        next: () => this.reload().subscribe(),
      });
    } else if (this._status() === 'electoral-justice') {
      this.http.post<AffiliationResponseDto>(`${this.membershipApiBase}/affiliations/${id}/confirm`, {}).subscribe({
        next: () => this.reload().subscribe(),
      });
    }
  }

  /** Resets the demo view only — there is no real "undo my affiliation" backend action. */
  reset(): void {
    this._affiliationId.set(null);
    this._status.set('not-started');
    this._card.set(null);
    this._fees.set([]);
  }

  payFee(id: string): void {
    const affiliationId = this._affiliationId();
    const fee = this._fees().find((f) => f.id === id);
    if (!affiliationId || !fee) {
      return;
    }
    const affiliation$ = this.http.get<AffiliationResponseDto>(`${this.membershipApiBase}/affiliations/${affiliationId}`);
    affiliation$
      .pipe(
        switchMap((affiliation) =>
          this.http.post<PaymentIntentResponseDto>(`${this.paymentsApiBase}/payment-intents`, {
            purpose: 'membership_fee',
            referenceId: id,
            payeeId: affiliation.partyId,
            amountCents: Math.round(fee.amount * 100),
            gateway: 'pix',
            idempotencyKey: `fee-${id}`,
          }),
        ),
        switchMap((intent) => this.http.post<PaymentIntentResponseDto>(`${this.paymentsApiBase}/payment-intents/${intent.id}/capture`, {})),
      )
      .subscribe({
        next: () => this.reload().subscribe(),
      });
  }
}
