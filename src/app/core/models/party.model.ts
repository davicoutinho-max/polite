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
  /** UF or município, when known — without it, a party with many representatives holding the
   * same office (e.g. 60 "Vereador" entries) reads as an undifferentiated repeated list. */
  readonly location: string;
}

export interface Party {
  readonly id: string;
  readonly name: string;
  readonly acronym: string;
  readonly number: number;
  readonly logoUrl: string;
  /** Empty string means no cover has been set — never auto-filled from the logo, so the neutral
   * placeholder gradient renders instead of stretching the party's own logo across the banner. */
  readonly coverUrl: string;
  /** Empty string means no presentation video has been set. May be a YouTube link or a direct
   * video file URL — see extractYouTubeId's javadoc for how the party page tells them apart. */
  readonly videoUrl: string;
  readonly ideology: string;
  /** Null when unknown — see PartySummary.founded's javadoc for why this is never faked. */
  readonly foundedYear: number | null;
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
