import { StatusTag } from './tag.model';

export interface PartyDirectory {
  readonly scope: 'Nacional' | 'Estadual' | 'Municipal';
  readonly location: string;
  readonly leader: string;
  readonly members: number;
}

export interface PartyEvent {
  readonly id: string;
  readonly title: string;
  readonly date: string;
  readonly location: string;
  readonly tag: StatusTag;
}

export interface PartyRepresentative {
  readonly id: string;
  readonly name: string;
  readonly role: string;
  readonly avatarUrl: string;
}

export interface Party {
  readonly id: string;
  readonly name: string;
  readonly acronym: string;
  readonly number: number;
  readonly logoUrl: string;
  readonly coverUrl: string;
  readonly ideology: string;
  readonly foundedYear: number;
  readonly president: string;
  readonly memberCount: number;
  readonly history: string;
  readonly program: string;
  readonly statuteUrl: string;
  readonly directories: PartyDirectory[];
  readonly events: PartyEvent[];
  readonly representatives: PartyRepresentative[];
}

/** A membership request seen by the party admin panel. */
export interface FiliationRequestSummary {
  readonly id: string;
  readonly name: string;
  readonly city: string;
  readonly requestedAt: string;
  readonly avatarUrl: string;
  status: 'pending' | 'approved' | 'rejected';
}

/** An already-affiliated member, as seen by the party admin panel. */
export interface PartyMemberSummary {
  readonly id: string;
  readonly name: string;
  readonly city: string;
  readonly avatarUrl: string;
  readonly joinedAt: string;
  status: 'active' | 'suspended';
}
