/** Government level a politician holds office at. */
export type GovLevel = 'federal' | 'state' | 'municipal';

/** Ideological spectrum bucket used to filter parties. */
export type PartySpectrum = 'left' | 'center-left' | 'center' | 'center-right' | 'right';

/** Compact politician record for the directory listing. */
export interface PoliticianSummary {
  readonly id: string;
  readonly name: string;
  readonly handle: string;
  readonly avatarUrl: string | null;
  readonly coverImageUrl: string | null;
  readonly verified: boolean;
  readonly office: string;
  readonly level: GovLevel;
  readonly partyId: string;
  readonly partyAcronym: string;
  readonly state: string;
  readonly followers: number;
  readonly billsCount: number;
}

/** Compact party record for the directory listing. */
export interface PartySummary {
  readonly id: string;
  readonly name: string;
  readonly acronym: string;
  readonly number: number;
  readonly logoUrl: string | null;
  readonly spectrum: PartySpectrum;
  readonly ideology: string;
  readonly members: number;
  /** Null when the government source has no founding date on file (confirmed: Câmara's party
   * endpoint doesn't expose one at all) — never fabricated as "today's year", which used to read
   * as an obviously wrong founding date for every party synced this way. */
  readonly founded: number | null;
  readonly president: string;
}

/** Fields a party fills in to register a new politician under itself. */
export interface NewPoliticianInput {
  readonly name: string;
  readonly handle: string;
  readonly cpf: string;
  readonly cnpj: string;
  readonly position: string;
  readonly level: GovLevel;
  readonly state: string;
  readonly partyId: string;
  readonly partyAcronym: string;
  readonly avatarUrl?: string;
}
