import { computed, Injectable, signal } from '@angular/core';
import { GovLevel, NewPoliticianInput, PartySpectrum, PartySummary, PoliticianSummary } from '../models';
import { PARTY_SEED, POLITICIAN_SEED } from '../data/directory-seed';

const DEFAULT_AVATAR =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuARrPtz8cdX-XEjmt6mzDr-yEB20GT86Vn2pwKXi-5JWa3WGOtpu2UZ53Clzs2UcsoUoRwjb6wjw4AUdOdgkX173o7MCsccQ_OhfJNR75fdsj3a5mJYH-bXhcLbpBI1-z4fVeifFnFeEQQKMdwNjq0xdG4H2KkmEDaK3ibUiLFVAb-mCXTgCg2zRPjR05v5YuxVH-JTO2o9dQN3hagJW1O1M_Tkor9T5VNVg8T-Ui3Hh1LYLnroVzxx0g';

export interface FilterOption {
  readonly value: string;
  readonly label: string;
}

export const LEVEL_OPTIONS: readonly { value: GovLevel; label: string }[] = [
  { value: 'federal', label: 'Federal' },
  { value: 'state', label: 'State' },
  { value: 'municipal', label: 'Municipal' },
];

export const SPECTRUM_OPTIONS: readonly { value: PartySpectrum; label: string }[] = [
  { value: 'left', label: 'Left' },
  { value: 'center-left', label: 'Center-left' },
  { value: 'center', label: 'Center' },
  { value: 'center-right', label: 'Center-right' },
  { value: 'right', label: 'Right' },
];

/** Source for the politician and party directory pages. Politicians are
 * mutable so parties can register new ones from Party Admin. */
@Injectable({ providedIn: 'root' })
export class DirectoryService {
  private readonly _politicians = signal<PoliticianSummary[]>(POLITICIAN_SEED);
  readonly politicians = this._politicians.asReadonly();
  readonly parties: readonly PartySummary[] = PARTY_SEED;

  readonly partyOptions: readonly FilterOption[] = PARTY_SEED.map((p) => ({
    value: p.id,
    label: `${p.name} (${p.acronym})`,
  }));

  readonly stateOptions = computed<FilterOption[]>(() =>
    [...new Set(this._politicians().map((p) => p.state))]
      .sort()
      .map((s) => ({ value: s, label: s })),
  );

  addPolitician(input: NewPoliticianInput): PoliticianSummary {
    const entry: PoliticianSummary = {
      id: `pol-${Date.now()}`,
      name: input.name,
      handle: input.handle,
      avatarUrl: input.avatarUrl ?? DEFAULT_AVATAR,
      verified: false,
      office: input.position,
      level: input.level,
      partyId: input.partyId,
      partyAcronym: input.partyAcronym,
      state: input.state,
      followers: 0,
      billsCount: 0,
    };
    this._politicians.update((list) => [...list, entry]);
    return entry;
  }
}
