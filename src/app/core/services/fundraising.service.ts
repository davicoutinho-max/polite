import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Fundraiser, FundraiserCategory, FundraiserCategoryMeta, NewFundraiser } from '../models';
import { DirectoryService } from './directory.service';
import { SessionService } from './session.service';

interface FundraiserResponseDto {
  readonly id: string;
  readonly organizerAccountId: string;
  readonly title: string;
  readonly description: string | null;
  readonly category: string;
  readonly goalCents: number;
  readonly raisedCents: number;
  readonly supportersCount: number;
  readonly deadline: string | null;
  readonly ledgerPublic: boolean;
}

interface AccountResponseDto {
  readonly name: string;
}

interface PaymentIntentResponseDto {
  readonly id: string;
}

@Injectable({ providedIn: 'root' })
export class FundraisingService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly directory = inject(DirectoryService);
  private readonly apiBase = `${environment.apiBaseUrl}/api/fundraising`;
  private readonly identityApiBase = `${environment.apiBaseUrl}/api/identity`;
  private readonly paymentsApiBase = `${environment.apiBaseUrl}/api/payments`;

  private readonly organizerCache = new Map<string, string>();

  readonly categories: FundraiserCategoryMeta[] = [
    { category: 'social', label: 'Social cause', icon: 'diversity_3' },
    { category: 'party', label: 'Party initiative', icon: 'flag' },
    { category: 'humanitarian', label: 'Humanitarian aid', icon: 'volunteer_activism' },
  ];

  private readonly _fundraisers = signal<Fundraiser[]>([]);
  readonly fundraisers = this._fundraisers.asReadonly();

  readonly totalRaised = computed(() => this._fundraisers().reduce((sum, f) => sum + f.raised, 0));
  readonly totalSupporters = computed(() => this._fundraisers().reduce((sum, f) => sum + f.supporters, 0));

  constructor() {
    this.reload().subscribe();
  }

  reload(page = 0, pageSize = 50): Observable<Fundraiser[]> {
    return this.http.get<FundraiserResponseDto[]>(`${this.apiBase}/fundraisers`, { params: { page, pageSize } }).pipe(
      switchMap((list) => (list.length ? forkJoin(list.map((dto) => this.toFundraiser(dto))) : of([]))),
      tap((fundraisers) => this._fundraisers.set(fundraisers)),
    );
  }

  create(input: NewFundraiser): void {
    this.http
      .post<FundraiserResponseDto>(`${this.apiBase}/fundraisers`, {
        title: input.title,
        description: input.description,
        category: input.category,
        goalCents: Math.round(input.goal * 100),
        deadline: input.deadline || null,
        ledgerPublic: true,
      })
      .pipe(switchMap((dto) => this.toFundraiser(dto)))
      .subscribe({
        next: (fundraiser) => this._fundraisers.update((list) => [fundraiser, ...list]),
      });
  }

  contribute(id: string, amount: number): void {
    if (amount <= 0) {
      return;
    }
    const fundraiser = this._fundraisers().find((f) => f.id === id);
    if (!fundraiser) {
      return;
    }
    this.http
      .get<FundraiserResponseDto>(`${this.apiBase}/fundraisers/${id}`)
      .pipe(
        switchMap((dto) =>
          this.http.post<PaymentIntentResponseDto>(`${this.paymentsApiBase}/payment-intents`, {
            purpose: 'fundraising_contribution',
            referenceId: id,
            payeeId: dto.organizerAccountId,
            amountCents: Math.round(amount * 100),
            gateway: 'pix',
            idempotencyKey: `contribution-${id}-${Date.now()}`,
          }),
        ),
        switchMap((intent) => this.http.post(`${this.paymentsApiBase}/payment-intents/${intent.id}/capture`, {})),
        switchMap(() => this.http.get<FundraiserResponseDto>(`${this.apiBase}/fundraisers/${id}`)),
        switchMap((dto) => this.toFundraiser(dto)),
      )
      .subscribe({
        next: (updated) => this._fundraisers.update((list) => list.map((f) => (f.id === id ? updated : f))),
      });
  }

  categoryMeta(category: FundraiserCategory): FundraiserCategoryMeta {
    return this.categories.find((c) => c.category === category) ?? this.categories[0];
  }

  private toFundraiser(dto: FundraiserResponseDto): Observable<Fundraiser> {
    return this.resolveOrganizerName(dto.organizerAccountId).pipe(
      map(
        (organizer): Fundraiser => ({
          id: dto.id,
          title: dto.title,
          description: dto.description ?? '',
          organizer,
          category: dto.category as FundraiserCategory,
          goal: dto.goalCents / 100,
          raised: dto.raisedCents / 100,
          supporters: dto.supportersCount,
          deadline: dto.deadline ?? 'Open-ended',
          ledgerPublic: dto.ledgerPublic,
        }),
      ),
    );
  }

  private resolveOrganizerName(accountId: string): Observable<string> {
    const cached = this.organizerCache.get(accountId);
    if (cached) {
      return of(cached);
    }
    const politician = this.directory.politicians().find((p) => p.id === accountId);
    if (politician) {
      this.organizerCache.set(accountId, politician.name);
      return of(politician.name);
    }
    const party = this.directory.parties().find((p) => p.id === accountId);
    if (party) {
      this.organizerCache.set(accountId, party.name);
      return of(party.name);
    }
    if (accountId === this.session.account().id) {
      return of(this.session.account().name);
    }
    return this.http.get<AccountResponseDto>(`${this.identityApiBase}/accounts/${accountId}`).pipe(
      map((r) => r.name),
      tap((name) => this.organizerCache.set(accountId, name)),
      catchError(() => of('Unknown organizer')),
    );
  }
}
