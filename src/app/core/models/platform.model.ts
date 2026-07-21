/** A party in the platform registry. Only platform admins create/edit these. */
export interface PartyRegistryEntry {
  readonly id: string;
  name: string;
  acronym: string;
  number: number;
  president: string;
  ideology: string;
  readonly memberCount: number;
}

/** A politician known to the platform, optionally linked to a party. */
export interface PoliticianDirectoryEntry {
  readonly id: string;
  readonly name: string;
  readonly office: string;
  /** Party id assigned by a platform admin, or null when independent. */
  partyId: string | null;
}

export interface NewParty {
  readonly name: string;
  readonly acronym: string;
  readonly number: number;
  readonly president: string;
  readonly ideology: string;
}

/** A country recognized by the platform, used to scope states and politicians. */
export interface PlatformCountry {
  readonly id: string;
  name: string;
  readonly code: string;
}

/** A state/province within a platform country. */
export interface PlatformState {
  readonly id: string;
  name: string;
  readonly code: string;
  countryId: string;
}

/** A language the platform can be presented in. Exactly one is the default. */
export interface PlatformLanguage {
  readonly id: string;
  name: string;
  readonly code: string;
  isDefault: boolean;
}

/** A parametrized elected office (cargo) offered when registering a politician — e.g.
 * "Vereador", "Deputado Federal". Managed by platform admins, not hardcoded. */
export interface PoliticalPosition {
  readonly id: string;
  readonly name: string;
}

/** A translation key and its value in every registered language, keyed by language id. */
export interface TranslationEntry {
  readonly id: string;
  key: string;
  values: Record<string, string>;
}
