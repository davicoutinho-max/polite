import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  FiliationRequestSummary,
  Party,
  PartyDirectory,
  PartyEvent,
  PartyMemberSummary,
  PartyRepresentative,
  PoliticianSummary,
  TagSeverity,
} from '../models';
import { relativeTime } from '../utils/relative-time';
import { DirectoryService } from './directory.service';

export interface RegisterPoliticianInput {
  readonly name: string;
  readonly handle: string;
  readonly email: string;
  readonly password: string;
  readonly documentNumber: string;
  readonly roleTitle: string;
  readonly state: string;
}

interface RepresentativeResponseDto {
  readonly id: string;
  readonly partyId: string;
  readonly politicianAccountId: string;
  readonly roleTitle: string | null;
  readonly linkedAt: string;
}

interface PartyProfileResponseDto {
  readonly history: string | null;
  readonly program: string | null;
  readonly statuteUrl: string | null;
  readonly coverUrl: string | null;
}

interface OfficeResponseDto {
  readonly scope: string;
  readonly location: string;
  readonly leaderName: string | null;
  readonly memberCount: number;
}

interface EventResponseDto {
  readonly id: string;
  readonly title: string;
  readonly eventDate: string;
  readonly location: string | null;
  readonly tagLabel: string | null;
  readonly tagSeverity: string | null;
}

interface AffiliationRequestResponseDto {
  readonly id: string;
  readonly citizenAccountId: string;
  readonly city: string | null;
  readonly status: string;
  readonly requestedAt: string;
}

interface PartyMemberResponseDto {
  readonly citizenAccountId: string;
  readonly city: string | null;
  readonly status: string;
  readonly joinedAt: string;
}

interface AccountResponseDto {
  readonly name: string;
  readonly avatarUrl: string;
}

const FALLBACK_AVATAR =
  'data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' viewBox=\'0 0 40 40\'%3E%3Crect width=\'40\' height=\'40\' fill=\'%23c7ccd1\'/%3E%3Ccircle cx=\'20\' cy=\'15\' r=\'7\' fill=\'%23fff\'/%3E%3Cpath d=\'M6 38c0-8 6-13 14-13s14 5 14 13z\' fill=\'%23fff\'/%3E%3C/svg%3E';

const EMPTY_PARTY: Party = {
  id: '',
  name: '',
  acronym: '',
  number: 0,
  logoUrl: FALLBACK_AVATAR,
  coverUrl: FALLBACK_AVATAR,
  ideology: '',
  foundedYear: new Date().getFullYear(),
  president: '',
  memberCount: 0,
  history: '',
  program: '',
  statuteUrl: '#',
  directories: [],
  events: [],
  representatives: [],
};

function capitalizeScope(scope: string): PartyDirectory['scope'] {
  return (scope.charAt(0).toUpperCase() + scope.slice(1)) as PartyDirectory['scope'];
}

/**
 * Party profile (public + admin) and party-admin panel state. Public identity fields (name,
 * acronym, logo, ideology, president, member count) come from DirectoryService's already-loaded
 * cache; everything else — profile prose, offices, events, representatives, affiliation
 * requests, member roster — comes from party-management-service. Citizen display names for
 * requests/members are resolved against identity-service (citizens aren't in directory-service).
 */
@Injectable({ providedIn: 'root' })
export class PartyService {
  private readonly http = inject(HttpClient);
  private readonly directory = inject(DirectoryService);
  private readonly apiBase = `${environment.apiBaseUrl}/api/party-management`;
  private readonly identityApiBase = `${environment.apiBaseUrl}/api/identity`;

  private readonly _party = signal<Party>(EMPTY_PARTY);
  readonly party = this._party.asReadonly();

  private readonly _requests = signal<FiliationRequestSummary[]>([]);
  readonly requests = this._requests.asReadonly();
  readonly pendingRequests = computed(() => this._requests().filter((r) => r.status === 'pending'));

  private readonly _members = signal<PartyMemberSummary[]>([]);
  readonly members = this._members.asReadonly();

  /** Loads the party's public profile, offices, events and representatives.
   *
   * Also re-fetches directory-service's politicians/parties caches (rather than trusting
   * whatever DirectoryService's own constructor-time load happened to have loaded by now) —
   * `toRepresentative`/the party summary lookup below read those signals synchronously, and
   * DirectoryService's initial fetch races this one on first navigation. Without this, arriving
   * here before that fetch resolves permanently baked "Unknown" into every representative's name,
   * since the mapping runs once and never re-resolves. */
  load(partyId: string): Observable<Party> {
    return forkJoin({
      profile: this.http.get<PartyProfileResponseDto>(`${this.apiBase}/parties/${partyId}/profile`),
      offices: this.http.get<OfficeResponseDto[]>(`${this.apiBase}/parties/${partyId}/offices`),
      events: this.http.get<EventResponseDto[]>(`${this.apiBase}/parties/${partyId}/events`),
      representatives: this.http.get<RepresentativeResponseDto[]>(`${this.apiBase}/parties/${partyId}/representatives`),
      directoryPoliticians: this.directory.reloadPoliticians(),
      directoryParties: this.directory.reloadParties(),
    }).pipe(
      map(({ profile, offices, events, representatives }): Party => {
        const summary = this.directory.parties().find((p) => p.id === partyId);
        return {
          id: partyId,
          name: summary?.name ?? '',
          acronym: summary?.acronym ?? '',
          number: summary?.number ?? 0,
          logoUrl: summary?.logoUrl || FALLBACK_AVATAR,
          coverUrl: profile.coverUrl || summary?.logoUrl || FALLBACK_AVATAR,
          ideology: summary?.ideology ?? '',
          foundedYear: summary?.founded ?? new Date().getFullYear(),
          president: summary?.president ?? '',
          memberCount: summary?.members ?? 0,
          history: profile.history ?? '',
          program: profile.program ?? '',
          statuteUrl: profile.statuteUrl || '#',
          directories: offices.map(
            (o): PartyDirectory => ({ scope: capitalizeScope(o.scope), location: o.location, leader: o.leaderName ?? '—', members: o.memberCount }),
          ),
          events: events.map(
            (e): PartyEvent => ({
              id: e.id,
              title: e.title,
              date: e.eventDate,
              location: e.location ?? '',
              tag: { label: e.tagLabel ?? '', severity: (e.tagSeverity as TagSeverity) ?? 'neutral' },
            }),
          ),
          representatives: representatives.map((r) => this.toRepresentative(r)),
        };
      }),
      tap((party) => this._party.set(party)),
    );
  }

  reloadRequests(partyId: string): Observable<FiliationRequestSummary[]> {
    return this.http.get<AffiliationRequestResponseDto[]>(`${this.apiBase}/parties/${partyId}/affiliation-requests`).pipe(
      switchMap((list) =>
        list.length
          ? forkJoin(
              list.map((r) =>
                this.resolveCitizen(r.citizenAccountId).pipe(
                  map(
                    (citizen): FiliationRequestSummary => ({
                      id: r.id,
                      name: citizen.name,
                      city: r.city ?? '',
                      requestedAt: relativeTime(r.requestedAt),
                      avatarUrl: citizen.avatarUrl,
                      status: r.status as FiliationRequestSummary['status'],
                    }),
                  ),
                ),
              ),
            )
          : of([]),
      ),
      tap((requests) => this._requests.set(requests)),
    );
  }

  reloadMembers(partyId: string): Observable<PartyMemberSummary[]> {
    return this.http.get<PartyMemberResponseDto[]>(`${this.apiBase}/parties/${partyId}/members`).pipe(
      switchMap((list) =>
        list.length
          ? forkJoin(
              list.map((m) =>
                this.resolveCitizen(m.citizenAccountId).pipe(
                  map(
                    (citizen): PartyMemberSummary => ({
                      id: m.citizenAccountId,
                      name: citizen.name,
                      city: m.city ?? '',
                      avatarUrl: citizen.avatarUrl,
                      joinedAt: relativeTime(m.joinedAt),
                      status: m.status as PartyMemberSummary['status'],
                    }),
                  ),
                ),
              ),
            )
          : of([]),
      ),
      tap((members) => this._members.set(members)),
    );
  }

  approveRequest(requestId: string): void {
    this.http.post(`${this.apiBase}/affiliation-requests/${requestId}/approve`, {}).subscribe({
      next: () => this.setRequestStatus(requestId, 'approved'),
    });
  }

  rejectRequest(requestId: string): void {
    this.http.post(`${this.apiBase}/affiliation-requests/${requestId}/reject`, {}).subscribe({
      next: () => this.setRequestStatus(requestId, 'rejected'),
    });
  }

  private setRequestStatus(id: string, status: FiliationRequestSummary['status']): void {
    this._requests.update((list) => list.map((r) => (r.id === id ? { ...r, status } : r)));
  }

  toggleMemberStatus(citizenAccountId: string): void {
    const member = this._members().find((m) => m.id === citizenAccountId);
    if (!member) {
      return;
    }
    const partyId = this._party().id;
    const newStatus: PartyMemberSummary['status'] = member.status === 'active' ? 'suspended' : 'active';
    this.http.patch(`${this.apiBase}/parties/${partyId}/members/${citizenAccountId}/status`, { status: newStatus }).subscribe({
      next: () => this._members.update((list) => list.map((m) => (m.id === citizenAccountId ? { ...m, status: newStatus } : m))),
    });
  }

  addRepresentative(candidate: PoliticianSummary): void {
    const partyId = this._party().id;
    this.http
      .post<RepresentativeResponseDto>(`${this.apiBase}/parties/${partyId}/representatives`, {
        politicianAccountId: candidate.id,
        roleTitle: candidate.office,
      })
      .subscribe({
        next: () => {
          const rep: PartyRepresentative = { id: candidate.id, name: candidate.name, role: candidate.office, avatarUrl: candidate.avatarUrl };
          this._party.update((party) =>
            party.representatives.some((r) => r.id === candidate.id) ? party : { ...party, representatives: [...party.representatives, rep] },
          );
        },
      });
  }

  removeRepresentative(politicianAccountId: string): void {
    const partyId = this._party().id;
    this.http.delete<void>(`${this.apiBase}/parties/${partyId}/representatives/${politicianAccountId}`).subscribe({
      next: () =>
        this._party.update((party) => ({ ...party, representatives: party.representatives.filter((r) => r.id !== politicianAccountId) })),
    });
  }

  createEvent(title: string, eventDate: string, location: string, tagLabel: string, tagSeverity: TagSeverity): Observable<PartyEvent> {
    const partyId = this._party().id;
    return this.http
      .post<EventResponseDto>(`${this.apiBase}/parties/${partyId}/events`, { title, eventDate, location, tagLabel, tagSeverity })
      .pipe(
        map(
          (e): PartyEvent => ({
            id: e.id,
            title: e.title,
            date: e.eventDate,
            location: e.location ?? '',
            tag: { label: e.tagLabel ?? '', severity: (e.tagSeverity as TagSeverity) ?? 'neutral' },
          }),
        ),
        tap((event) => this._party.update((party) => ({ ...party, events: [...party.events, event] }))),
      );
  }

  /** Real registration against party-management-service — creates the politician's
   * authenticatable identity AND links it to this party in one call (see
   * RegisterPoliticianService.registerPolitician's javadoc). On success the new representative
   * is added to the local party signal immediately; the caller should also refresh
   * DirectoryService so the new politician shows up in the public directory. */
  registerPolitician(partyId: string, input: RegisterPoliticianInput): Observable<RepresentativeResponseDto> {
    return this.http
      .post<RepresentativeResponseDto>(`${this.apiBase}/parties/${partyId}/representatives/register`, {
        name: input.name,
        handle: input.handle,
        email: input.email,
        password: input.password,
        documentType: 'cpf',
        documentNumber: input.documentNumber,
        roleTitle: input.roleTitle,
        state: input.state,
      })
      .pipe(
        tap((response) => {
          const rep: PartyRepresentative = { id: response.politicianAccountId, name: input.name, role: input.roleTitle, avatarUrl: '' };
          this._party.update((party) => ({ ...party, representatives: [...party.representatives, rep] }));
        }),
      );
  }

  private toRepresentative(r: RepresentativeResponseDto): PartyRepresentative {
    const politician = this.directory.politicians().find((p) => p.id === r.politicianAccountId);
    return {
      id: r.politicianAccountId,
      name: politician?.name ?? 'Unknown',
      role: r.roleTitle ?? politician?.office ?? '',
      avatarUrl: politician?.avatarUrl || FALLBACK_AVATAR,
    };
  }

  private resolveCitizen(accountId: string): Observable<{ name: string; avatarUrl: string }> {
    return this.http.get<AccountResponseDto>(`${this.identityApiBase}/accounts/${accountId}`).pipe(
      map((r) => ({ name: r.name, avatarUrl: r.avatarUrl || FALLBACK_AVATAR })),
    );
  }
}
