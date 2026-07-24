import { HttpClient } from '@angular/common/http';
import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { Observable, map, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { GovLevel, PartySpectrum, PartySummary, PoliticianSummary } from '../models';
import { SessionService } from './session.service';

const DEFAULT_AVATAR =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuARrPtz8cdX-XEjmt6mzDr-yEB20GT86Vn2pwKXi-5JWa3WGOtpu2UZ53Clzs2UcsoUoRwjb6wjw4AUdOdgkX173o7MCsccQ_OhfJNR75fdsj3a5mJYH-bXhcLbpBI1-z4fVeifFnFeEQQKMdwNjq0xdG4H2KkmEDaK3ibUiLFVAb-mCXTgCg2zRPjR05v5YuxVH-JTO2o9dQN3hagJW1O1M_Tkor9T5VNVg8T-Ui3Hh1LYLnroVzxx0g';

/** Loading a single large page once and filtering/sorting client-side (same pattern used
 * throughout this pass — see legislative-service's demo-scale reasoning) rather than wiring
 * server-side pagination through every directory page's filter UI. */
const DIRECTORY_PAGE_SIZE = 500;

export interface FilterOption {
  readonly value: string;
  readonly label: string;
}

export const SPECTRUM_OPTIONS: readonly { value: PartySpectrum; label: string }[] = [
  { value: 'left', label: 'Left' },
  { value: 'center-left', label: 'Center-left' },
  { value: 'center', label: 'Center' },
  { value: 'center-right', label: 'Center-right' },
  { value: 'right', label: 'Right' },
];

interface PoliticianResponse {
  readonly accountId: string;
  readonly name: string;
  readonly handle: string;
  readonly avatarUrl: string | null;
  readonly verified: boolean;
  readonly office: string | null;
  readonly level: string | null;
  readonly partyId: string | null;
  readonly partyAcronym: string | null;
  readonly state: string | null;
  readonly followersCount: number;
  readonly billsCount: number;
}

interface PartyResponse {
  readonly id: string;
  readonly name: string;
  readonly acronym: string;
  readonly number: number;
  readonly ideology: string | null;
  readonly spectrum: string | null;
  readonly foundedYear: number | null;
  readonly president: string | null;
  readonly logoUrl: string | null;
  readonly memberCount: number;
}

function toPoliticianSummary(response: PoliticianResponse): PoliticianSummary {
  return {
    id: response.accountId,
    name: response.name,
    handle: `@${response.handle}`,
    avatarUrl: response.avatarUrl ?? DEFAULT_AVATAR,
    verified: response.verified,
    office: response.office ?? '',
    level: (response.level as GovLevel) ?? 'federal',
    partyId: response.partyId ?? '',
    partyAcronym: response.partyAcronym ?? '',
    state: response.state ?? '',
    followers: response.followersCount,
    billsCount: response.billsCount,
  };
}

function toPartySummary(response: PartyResponse): PartySummary {
  return {
    id: response.id,
    name: response.name,
    acronym: response.acronym,
    number: response.number,
    logoUrl: response.logoUrl ?? DEFAULT_AVATAR,
    spectrum: (response.spectrum?.replace(/_/g, '-') as PartySpectrum) ?? 'center',
    ideology: response.ideology ?? '',
    members: response.memberCount,
    founded: response.foundedYear ?? new Date().getFullYear(),
    president: response.president ?? '',
  };
}

/** Source for the politician and party directory pages, and the platform's follow graph — all
 * real reads against directory-service. Politicians are only ever created by
 * party-management-service's registration flow (see party.service.ts), never by this service. */
@Injectable({ providedIn: 'root' })
export class DirectoryService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly apiBase = `${environment.apiBaseUrl}/api/directory`;

  private readonly _politicians = signal<PoliticianSummary[]>([]);
  readonly politicians = this._politicians.asReadonly();

  private readonly _parties = signal<PartySummary[]>([]);
  readonly parties = this._parties.asReadonly();

  private readonly _followingPoliticians = signal<ReadonlySet<string>>(new Set());
  readonly followingPoliticians = this._followingPoliticians.asReadonly();

  private readonly _followingParties = signal<ReadonlySet<string>>(new Set());
  readonly followingParties = this._followingParties.asReadonly();

  readonly partyOptions = computed<FilterOption[]>(() => this._parties().map((p) => ({ value: p.id, label: `${p.name} (${p.acronym})` })));

  readonly stateOptions = computed<FilterOption[]>(() =>
    [...new Set(this._politicians().map((p) => p.state).filter((s) => s))]
      .sort()
      .map((s) => ({ value: s, label: s })),
  );

  /** Derived from whatever office values politicians actually carry (e.g. "Senador", "Deputado
   * Federal", "City Councilor") rather than a hardcoded enum — real-world office titles vary too
   * much across levels/parties to enumerate up front, same reasoning as stateOptions above. */
  readonly officeOptions = computed<FilterOption[]>(() =>
    [...new Set(this._politicians().map((p) => p.office).filter((o) => o))]
      .sort()
      .map((o) => ({ value: o, label: o })),
  );

  constructor() {
    this.reloadPoliticians().subscribe();
    this.reloadParties().subscribe();
    // Gated on session.ready() rather than fired immediately: on a hard refresh the token-restore
    // in SessionService resolves asynchronously (see its constructor), so isAuthenticated() is
    // still false at this constructor's first tick. Firing here unconditionally used to silently
    // fetch an empty following set that was never retried, permanently desyncing every Follow
    // button from the real server-side state until the next full reload happened to race the
    // other way. The effect re-runs whenever `ready` flips, so it fires exactly once the real
    // auth state is known, whether that's on this tick or a moment later.
    effect(() => {
      if (this.session.ready()) {
        this.reloadFollowing('politician').subscribe();
        this.reloadFollowing('party').subscribe();
      }
    });
  }

  reloadPoliticians(): Observable<PoliticianSummary[]> {
    return this.http.get<PoliticianResponse[]>(`${this.apiBase}/politicians`, { params: { pageSize: DIRECTORY_PAGE_SIZE } }).pipe(
      map((list) => list.map(toPoliticianSummary)),
      tap((list) => this._politicians.set(list)),
    );
  }

  reloadParties(): Observable<PartySummary[]> {
    return this.http.get<PartyResponse[]>(`${this.apiBase}/parties`, { params: { pageSize: DIRECTORY_PAGE_SIZE } }).pipe(
      map((list) => list.map(toPartySummary)),
      tap((list) => this._parties.set(list)),
    );
  }

  getPolitician(accountId: string): Observable<PoliticianSummary> {
    return this.http.get<PoliticianResponse>(`${this.apiBase}/politicians/${accountId}`).pipe(map(toPoliticianSummary));
  }

  reloadFollowing(targetType: 'politician' | 'party'): Observable<string[]> {
    if (!this.session.isAuthenticated()) {
      return of([]);
    }
    return this.http.get<string[]>(`${this.apiBase}/follows`, { params: { targetType } }).pipe(
      tap((ids) => {
        const set = new Set(ids);
        if (targetType === 'politician') {
          this._followingPoliticians.set(set);
        } else {
          this._followingParties.set(set);
        }
      }),
    );
  }

  isFollowing(targetType: 'politician' | 'party', targetId: string): boolean {
    return targetType === 'politician' ? this._followingPoliticians().has(targetId) : this._followingParties().has(targetId);
  }

  follow(targetType: 'politician' | 'party', targetId: string): Observable<void> {
    return this.http.post<void>(`${this.apiBase}/follows`, { targetType, targetId }).pipe(
      tap(() => {
        this.addFollowingLocally(targetType, targetId);
        this.bumpCountLocally(targetType, targetId, 1);
      }),
    );
  }

  unfollow(targetType: 'politician' | 'party', targetId: string): Observable<void> {
    return this.http.request<void>('DELETE', `${this.apiBase}/follows`, { body: { targetType, targetId } }).pipe(
      tap(() => {
        this.removeFollowingLocally(targetType, targetId);
        this.bumpCountLocally(targetType, targetId, -1);
      }),
    );
  }

  private addFollowingLocally(targetType: 'politician' | 'party', targetId: string): void {
    const signalRef = targetType === 'politician' ? this._followingPoliticians : this._followingParties;
    signalRef.update((set) => new Set(set).add(targetId));
  }

  private removeFollowingLocally(targetType: 'politician' | 'party', targetId: string): void {
    const signalRef = targetType === 'politician' ? this._followingPoliticians : this._followingParties;
    signalRef.update((set) => {
      const next = new Set(set);
      next.delete(targetId);
      return next;
    });
  }

  /** Optimistic local follower/member count bump — the real count still gets confirmed on the
   * next `reloadPoliticians()`/`reloadParties()`, but without this the card the user is looking
   * straight at doesn't move until they reload the page. */
  private bumpCountLocally(targetType: 'politician' | 'party', targetId: string, delta: 1 | -1): void {
    if (targetType === 'politician') {
      this._politicians.update((list) => list.map((p) => (p.id === targetId ? { ...p, followers: p.followers + delta } : p)));
    } else {
      this._parties.update((list) => list.map((p) => (p.id === targetId ? { ...p, members: p.members + delta } : p)));
    }
  }
}
