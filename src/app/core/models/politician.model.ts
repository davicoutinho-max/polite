import { UserSummary } from './user.model';

export interface Mandate {
  readonly role: string;
  readonly period: string;
  readonly current?: boolean;
}

export interface SocialLink {
  readonly icon: string;
  readonly label: string;
  readonly handle: string;
  readonly url: string;
}

export interface TeamMember {
  readonly name: string;
  readonly role: string;
  readonly avatarUrl: string;
}

export interface Politician extends UserSummary {
  readonly party: string;
  readonly partyId: string;
  readonly position: string;
  readonly servingSince: number;
  readonly coverUrl: string;

  // Personal / civic dossier
  readonly education: string;
  readonly profession: string;
  readonly patrimony: string;
  readonly email: string;
  readonly phone: string;
  readonly office: string;

  readonly mandates: Mandate[];
  readonly socialLinks: SocialLink[];
  readonly team: TeamMember[];
}

export interface ProfileTab {
  readonly id: string;
  readonly label: string;
  readonly key: string;
  readonly icon: string;
}
