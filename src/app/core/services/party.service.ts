import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { catchError, Observable, finalize, forkJoin, map, of, switchMap, tap } from 'rxjs';
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

/** Redeeming a politician invite token (see PoliticianInvite's javadoc) — the politician's name/
 * role/state were already vetted by the party at invite time, not typed here. */
export interface RedeemPoliticianInviteInput {
  readonly registrationToken: string;
  readonly handle: string;
  readonly email: string;
  readonly password: string;
  readonly documentNumber: string;
}

/** A politician invite token issued from the party's own admin panel — replaces the old "party
 * types the new politician's password directly" flow. See identity-service's RegistrationToken
 * javadoc. */
export interface PoliticianInvite {
  readonly id: string;
  readonly token: string;
  readonly targetEmail: string | null;
  readonly status: 'pending' | 'consumed' | 'expired';
}

export interface NewPoliticianInviteInput {
  readonly name: string;
  readonly roleTitle: string;
  readonly state: string;
  readonly targetEmail: string;
}

interface PoliticianInviteResponseDto {
  readonly id: string;
  readonly token: string;
  readonly targetEmail: string | null;
  readonly status: string;
}

function toPoliticianInvite(dto: PoliticianInviteResponseDto): PoliticianInvite {
  return { id: dto.id, token: dto.token, targetEmail: dto.targetEmail, status: dto.status as PoliticianInvite['status'] };
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
  readonly videoUrl: string | null;
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
  coverUrl: '',
  videoUrl: '',
  ideology: '',
  foundedYear: null,
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
  private readonly _loading = signal(true);
  readonly loading = this._loading.asReadonly();

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
    this._loading.set(true);
    return forkJoin({
      profile: this.http.get<PartyProfileResponseDto>(`${this.apiBase}/parties/${partyId}/profile`),
      offices: this.http.get<OfficeResponseDto[]>(`${this.apiBase}/parties/${partyId}/offices`),
      events: this.http.get<EventResponseDto[]>(`${this.apiBase}/parties/${partyId}/events`),
      representatives: this.http.get<RepresentativeResponseDto[]>(`${this.apiBase}/parties/${partyId}/representatives`),
      directoryPoliticians: this.directory.reloadPoliticians(),
      directoryParties: this.directory.reloadParties(),
    }).pipe(
      switchMap(({ profile, offices, events, representatives }) => {
        const summary = this.directory.parties().find((p) => p.id === partyId);
        const base = {
          id: partyId,
          name: summary?.name ?? '',
          acronym: summary?.acronym ?? '',
          number: summary?.number ?? 0,
          logoUrl: summary?.logoUrl || FALLBACK_AVATAR,
          // Never falls back to the logo — an unset cover shows the neutral placeholder gradient
          // (party-page.html's .hero__cover--placeholder) instead of silently reusing the logo.
          coverUrl: profile.coverUrl || '',
          videoUrl: profile.videoUrl || '',
          ideology: summary?.ideology ?? '',
          foundedYear: summary?.founded ?? null,
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
        };
        return (representatives.length ? forkJoin(representatives.map((r) => this.resolveRepresentative(r))) : of([])).pipe(
          map((reps): Party => ({ ...base, representatives: reps })),
        );
      }),
      tap((party) => this._party.set(party)),
      finalize(() => this._loading.set(false)),
    );
  }

  /** Party logo (avatar-equivalent) — stored on directory-service's own Party row alongside
   * name/acronym/number, unlike the cover photo below. */
  updateLogo(logoUrl: string): Observable<void> {
    return this.directory.updatePartyLogo(logoUrl).pipe(tap(() => this._party.update((p) => ({ ...p, logoUrl }))));
  }

  /** Cover photo — a full replace on party-management-service's own editable profile record, so
   * every other field must be resent with its current value or it would be silently blanked. */
  updateCoverPhoto(coverUrl: string): Observable<void> {
    const party = this._party();
    return this.updateProfile(party.history, party.program, party.statuteUrl === '#' ? '' : party.statuteUrl, coverUrl, party.videoUrl);
  }

  /** Self-service party profile editing (history/program/statute/cover/video) — same full-replace
   * PUT as {@link updateCoverPhoto}, see its javadoc for why every field is always resent together. */
  updateProfile(history: string, program: string, statuteUrl: string, coverUrl?: string, videoUrl?: string): Observable<void> {
    const party = this._party();
    const resolvedCoverUrl = coverUrl ?? (party.coverUrl || null);
    const resolvedVideoUrl = videoUrl ?? (party.videoUrl || null);
    return this.http
      .put<PartyProfileResponseDto>(`${this.apiBase}/parties/${party.id}/profile`, {
        history: history || null,
        program: program || null,
        statuteUrl: statuteUrl || null,
        coverUrl: resolvedCoverUrl,
        videoUrl: resolvedVideoUrl,
      })
      .pipe(
        map(() => undefined),
        tap(() =>
          this._party.update((p) => ({
            ...p,
            history,
            program,
            statuteUrl: statuteUrl || '#',
            coverUrl: resolvedCoverUrl ?? p.coverUrl,
            videoUrl: resolvedVideoUrl ?? p.videoUrl,
          })),
        ),
      );
  }

  /** Self-service update of the party's own registry-style fields (name/acronym/number/ideology/
   * founded year/president) on directory-service — see that service's Party.updateDetails javadoc
   * for why this exists alongside the government-sync projection that normally owns them. */
  updatePartyDetails(
    name: string,
    acronym: string,
    number: number,
    ideology: string,
    foundedYear: number | null,
    president: string,
  ): Observable<void> {
    return this.directory.updatePartyDetails(name, acronym, number, ideology, foundedYear, president).pipe(
      tap(() => this._party.update((p) => ({ ...p, name, acronym, number, ideology, foundedYear, president }))),
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
          const rep: PartyRepresentative = {
            id: candidate.id,
            name: candidate.name,
            role: candidate.office,
            avatarUrl: candidate.avatarUrl ?? '',
            location: candidate.state ?? '',
          };
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

  /** `partyId` defaults to whatever party profile is currently loaded (the admin panel's own
   * usage) — pass it explicitly when creating an event for the signed-in party account without
   * necessarily having loaded its own profile first (e.g. from an agenda post in the main feed). */
  createEvent(
    title: string,
    eventDate: string,
    location: string,
    tagLabel: string,
    tagSeverity: TagSeverity,
    partyId?: string,
  ): Observable<PartyEvent> {
    const resolvedPartyId = partyId ?? this._party().id;
    return this.http
      .post<EventResponseDto>(`${this.apiBase}/parties/${resolvedPartyId}/events`, { title, eventDate, location, tagLabel, tagSeverity })
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
        tap((event) => {
          if (resolvedPartyId === this._party().id) {
            this._party.update((party) => ({ ...party, events: [...party.events, event] }));
          }
        }),
      );
  }

  /** Redeems a politician invite token against party-management-service — called from the
   * public register page (not the party admin panel, which only issues invites now, see
   * {@link issuePoliticianInvite}). Creates the politician's authenticatable identity AND links
   * it to {@code partyId} in one call (see RegisterPoliticianService.registerPolitician's
   * javadoc); the caller is typically anonymous at this point, so there's usually no locally
   * loaded party to update, but the optimistic update below is harmless either way. */
  registerPolitician(partyId: string, input: RedeemPoliticianInviteInput): Observable<RepresentativeResponseDto> {
    return this.http
      .post<RepresentativeResponseDto>(`${this.apiBase}/parties/${partyId}/representatives/register`, {
        registrationToken: input.registrationToken,
        handle: input.handle,
        email: input.email,
        password: input.password,
        documentType: 'cpf',
        documentNumber: input.documentNumber,
      })
      .pipe(
        tap((response) => {
          const rep: PartyRepresentative = {
            id: response.politicianAccountId,
            name: '',
            role: response.roleTitle ?? '',
            avatarUrl: '',
            location: '',
          };
          this._party.update((party) =>
            party.id === partyId ? { ...party, representatives: [...party.representatives, rep] } : party,
          );
        }),
      );
  }

  /** Party admin issuing a politician invite (name/roleTitle/state vetted here, emailed as a
   * token the politician's own contact redeems with their own password). */
  issuePoliticianInvite(partyId: string, input: NewPoliticianInviteInput): Observable<PoliticianInvite> {
    return this.http
      .post<PoliticianInviteResponseDto>(`${this.apiBase}/parties/${partyId}/politician-invites`, {
        name: input.name,
        roleTitle: input.roleTitle,
        state: input.state,
        targetEmail: input.targetEmail,
      })
      .pipe(map(toPoliticianInvite));
  }

  resendPoliticianInvite(partyId: string, id: string): Observable<PoliticianInvite> {
    return this.http.post<PoliticianInviteResponseDto>(`${this.apiBase}/parties/${partyId}/politician-invites/${id}/resend`, {}).pipe(map(toPoliticianInvite));
  }

  listPoliticianInvites(partyId: string): Observable<PoliticianInvite[]> {
    return this.http
      .get<PoliticianInviteResponseDto[]>(`${this.apiBase}/parties/${partyId}/politician-invites`)
      .pipe(map((list) => list.map(toPoliticianInvite)));
  }

  /** Resolves a representative against the already-loaded directory cache first; on a cache miss
   * (the id fell outside the directory page or hadn't synced yet when the cache was built) falls
   * back to fetching that one politician directly rather than ever showing "Unknown" — see
   * DirectoryService's DIRECTORY_PAGE_SIZE javadoc for why cache misses can happen at all. */
  private resolveRepresentative(r: RepresentativeResponseDto): Observable<PartyRepresentative> {
    const cached = this.directory.politicians().find((p) => p.id === r.politicianAccountId);
    if (cached) {
      return of({
        id: r.politicianAccountId,
        name: cached.name,
        role: r.roleTitle ?? cached.office ?? '',
        avatarUrl: cached.avatarUrl || FALLBACK_AVATAR,
        location: cached.state ?? '',
      });
    }
    return this.directory.getPolitician(r.politicianAccountId).pipe(
      map(
        (p): PartyRepresentative => ({
          id: r.politicianAccountId,
          name: p.name,
          role: r.roleTitle ?? p.office ?? '',
          avatarUrl: p.avatarUrl || FALLBACK_AVATAR,
          location: p.state ?? '',
        }),
      ),
      catchError(() => of({ id: r.politicianAccountId, name: 'Unknown', role: r.roleTitle ?? '', avatarUrl: FALLBACK_AVATAR, location: '' })),
    );
  }

  private resolveCitizen(accountId: string): Observable<{ name: string; avatarUrl: string }> {
    return this.http.get<AccountResponseDto>(`${this.identityApiBase}/accounts/${accountId}`).pipe(
      map((r) => ({ name: r.name, avatarUrl: r.avatarUrl || FALLBACK_AVATAR })),
    );
  }
}
