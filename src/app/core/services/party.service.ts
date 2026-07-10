import { computed, Injectable, signal } from '@angular/core';
import { FiliationRequestSummary, Party, PartyMemberSummary, PartyRepresentative, PoliticianSummary } from '../models';

const AVATAR =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuARrPtz8cdX-XEjmt6mzDr-yEB20GT86Vn2pwKXi-5JWa3WGOtpu2UZ53Clzs2UcsoUoRwjb6wjw4AUdOdgkX173o7MCsccQ_OhfJNR75fdsj3a5mJYH-bXhcLbpBI1-z4fVeifFnFeEQQKMdwNjq0xdG4H2KkmEDaK3ibUiLFVAb-mCXTgCg2zRPjR05v5YuxVH-JTO2o9dQN3hagJW1O1M_Tkor9T5VNVg8T-Ui3Hh1LYLnroVzxx0g';

@Injectable({ providedIn: 'root' })
export class PartyService {
  private readonly _party = signal<Party>({
    id: 'progressive',
    name: 'Progressive Party',
    acronym: 'PP',
    number: 45,
    logoUrl: AVATAR,
    coverUrl:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuBfLxYlq8drGMj6_MPWgFtzX7vBUdily37sBqP2qqDgu6Pr4snpjPwswLUuRi551U0HSSeo-ATVCth_kekH52TSm63uIHxtXhYT7DKWwah7JKJdtNm87kTmzW-PkNjenweQVV7ArrkSGdD65jKJziFdFB8A0egariUvsXLjqr56Bv0nJnp2fxm29q89UChlOGvGWHP3_RXCyqhOKrgwXMNtBgKLzVVAMZBz0JEIdys087x9l4pwXTYjxQ',
    ideology: 'Social democracy · Progressivism',
    foundedYear: 1998,
    president: 'Sen. Laura Prado',
    memberCount: 148230,
    history:
      'Founded in 1998, the Progressive Party grew from a coalition of civic movements advocating for public transparency, sustainable development and social equality across the country.',
    program:
      'A modern, accountable state: open public data, green infrastructure, universal access to education and healthcare, and citizen-driven participatory budgeting.',
    statuteUrl: '#',
    directories: [
      { scope: 'Nacional', location: 'Federal District', leader: 'Sen. Laura Prado', members: 148230 },
      { scope: 'Estadual', location: 'São Paulo', leader: 'Rep. Diego Faria', members: 32410 },
      { scope: 'Municipal', location: 'Campinas', leader: 'Councilor Rita Sá', members: 4120 },
    ],
    events: [
      { id: 'ev1', title: 'National Convention 2026', date: 'Aug 12, 2026', location: 'Brasília', tag: { label: 'Upcoming', severity: 'secondary' } },
      { id: 'ev2', title: 'Youth Leadership Course', date: 'Jul 20, 2026', location: 'Online', tag: { label: 'Enrolling', severity: 'success' } },
      { id: 'ev3', title: 'Municipal Directors Forum', date: 'Jul 15, 2026', location: 'São Paulo', tag: { label: 'This week', severity: 'warning' } },
    ],
    representatives: [
      { id: 'jane-doe', name: 'Jane Doe', role: 'Federal Deputy', avatarUrl: AVATAR },
      { id: 'marcus-chen', name: 'Marcus Chen', role: 'City Councilor', avatarUrl: AVATAR },
      { id: 'laura-prado', name: 'Laura Prado', role: 'Senator · President', avatarUrl: AVATAR },
    ],
  });
  readonly party = this._party.asReadonly();

  // ---- Admin panel: membership requests ----
  private readonly _requests = signal<FiliationRequestSummary[]>([
    { id: 'r1', name: 'Bruno Tavares', city: 'Campinas — SP', requestedAt: '2h ago', avatarUrl: AVATAR, status: 'pending' },
    { id: 'r2', name: 'Helena Costa', city: 'Santos — SP', requestedAt: '5h ago', avatarUrl: AVATAR, status: 'pending' },
    { id: 'r3', name: 'Igor Mendes', city: 'Ribeirão — SP', requestedAt: 'Yesterday', avatarUrl: AVATAR, status: 'pending' },
    { id: 'r4', name: 'Sofia Ramos', city: 'São Paulo — SP', requestedAt: '2 days ago', avatarUrl: AVATAR, status: 'approved' },
  ]);
  readonly requests = this._requests.asReadonly();
  readonly pendingRequests = computed(() => this._requests().filter((r) => r.status === 'pending'));

  approveRequest(id: string): void {
    this.setRequestStatus(id, 'approved');
  }

  rejectRequest(id: string): void {
    this.setRequestStatus(id, 'rejected');
  }

  private setRequestStatus(id: string, status: FiliationRequestSummary['status']): void {
    this._requests.update((list) => list.map((r) => (r.id === id ? { ...r, status } : r)));
  }

  // ---- Admin panel: affiliated members ----
  private readonly _members = signal<PartyMemberSummary[]>([
    { id: 'm1', name: 'Sofia Ramos', city: 'São Paulo — SP', avatarUrl: AVATAR, joinedAt: '2 days ago', status: 'active' },
    { id: 'm2', name: 'Carlos Andrade', city: 'Campinas — SP', avatarUrl: AVATAR, joinedAt: '1 week ago', status: 'active' },
    { id: 'm3', name: 'Beatriz Lima', city: 'Santos — SP', avatarUrl: AVATAR, joinedAt: '2 weeks ago', status: 'active' },
    { id: 'm4', name: 'Rafael Souza', city: 'Ribeirão — SP', avatarUrl: AVATAR, joinedAt: '1 month ago', status: 'suspended' },
    { id: 'm5', name: 'Camila Duarte', city: 'São Paulo — SP', avatarUrl: AVATAR, joinedAt: '2 months ago', status: 'active' },
    { id: 'm6', name: 'Thiago Nunes', city: 'Brasília — DF', avatarUrl: AVATAR, joinedAt: '3 months ago', status: 'active' },
  ]);
  readonly members = this._members.asReadonly();

  toggleMemberStatus(id: string): void {
    this._members.update((list) =>
      list.map((m) => (m.id === id ? { ...m, status: m.status === 'active' ? 'suspended' : 'active' } : m)),
    );
  }

  // ---- Admin panel: politicians linked to the party ----
  addRepresentative(candidate: PoliticianSummary): void {
    this._party.update((party) => {
      if (party.representatives.some((r) => r.id === candidate.id)) return party;
      const rep: PartyRepresentative = { id: candidate.id, name: candidate.name, role: candidate.office, avatarUrl: candidate.avatarUrl };
      return { ...party, representatives: [...party.representatives, rep] };
    });
  }

  removeRepresentative(id: string): void {
    this._party.update((party) => ({ ...party, representatives: party.representatives.filter((r) => r.id !== id) }));
  }
}
