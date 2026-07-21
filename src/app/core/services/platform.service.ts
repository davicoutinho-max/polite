import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { DirectoryService } from './directory.service';
import {
  NewParty,
  PartyRegistryEntry,
  PlatformCountry,
  PlatformLanguage,
  PlatformState,
  PoliticalPosition,
  PoliticianDirectoryEntry,
  TranslationEntry,
} from '../models';

export interface NewPartyInput extends NewParty {
  readonly handle: string;
  readonly email: string;
  readonly password: string;
  readonly documentNumber: string;
}

interface PartyRegistryResponse {
  readonly id: string;
  readonly name: string;
  readonly acronym: string;
  readonly number: number;
  readonly president: string | null;
  readonly ideology: string | null;
  readonly memberCount: number;
}

interface CountryResponse {
  readonly id: string;
  readonly name: string;
  readonly code: string;
}

interface StateResponse {
  readonly id: string;
  readonly countryId: string;
  readonly name: string;
  readonly code: string;
}

interface PoliticalPositionResponse {
  readonly id: string;
  readonly name: string;
}

interface LanguageResponse {
  readonly id: string;
  readonly name: string;
  readonly code: string;
  readonly isDefault: boolean;
}

interface TranslationKeyResponse {
  readonly id: string;
  readonly key: string;
}

interface TranslationValueResponse {
  readonly translationKeyId: string;
  readonly languageId: string;
  readonly value: string;
}

function toPartyRegistryEntry(response: PartyRegistryResponse): PartyRegistryEntry {
  return {
    id: response.id,
    name: response.name,
    acronym: response.acronym,
    number: response.number,
    president: response.president ?? '—',
    ideology: response.ideology ?? '—',
    memberCount: response.memberCount,
  };
}

function toPlatformCountry(response: CountryResponse): PlatformCountry {
  return { id: response.id, name: response.name, code: response.code };
}

function toPlatformState(response: StateResponse): PlatformState {
  return { id: response.id, name: response.name, code: response.code, countryId: response.countryId };
}

function toPlatformLanguage(response: LanguageResponse): PlatformLanguage {
  return { id: response.id, name: response.name, code: response.code, isDefault: response.isDefault };
}

/**
 * Platform-administration state: the party registry, politician assignment, geography
 * parameters, and the language/translation-tag registry — all real reads/writes against
 * platform-configuration-service (politicians themselves come from DirectoryService, since
 * platform-configuration-service only owns the party-assignment relationship, not the
 * politician roster). TranslateService reads `translations()` directly, so the app's
 * `| translate` pipe is backed by this same real registry.
 */
@Injectable({ providedIn: 'root' })
export class PlatformService {
  private readonly http = inject(HttpClient);
  private readonly directoryService = inject(DirectoryService);
  private readonly apiBase = `${environment.apiBaseUrl}/api/platform`;

  private readonly _parties = signal<PartyRegistryEntry[]>([]);
  readonly parties = this._parties.asReadonly();

  readonly politicians = computed<PoliticianDirectoryEntry[]>(() =>
    this.directoryService.politicians().map((p) => ({ id: p.id, name: p.name, office: p.office, partyId: p.partyId || null })),
  );

  readonly unassigned = computed(() => this.politicians().filter((p) => p.partyId === null));

  constructor() {
    this.reloadParties().subscribe();
    this.reloadCountries().subscribe();
    this.reloadLanguages().subscribe();
    this.reloadPoliticalPositions().subscribe();
  }

  reloadParties(): Observable<PartyRegistryEntry[]> {
    return this.http.get<PartyRegistryResponse[]>(`${this.apiBase}/parties`).pipe(
      map((list) => list.map(toPartyRegistryEntry)),
      tap((list) => this._parties.set(list)),
    );
  }

  partyName(id: string | null): string {
    if (!id) {
      return 'Independent';
    }
    return this._parties().find((p) => p.id === id)?.name ?? 'Independent';
  }

  politiciansOf(partyId: string): PoliticianDirectoryEntry[] {
    return this.politicians().filter((p) => p.partyId === partyId);
  }

  createParty(input: NewPartyInput): Observable<PartyRegistryEntry> {
    return this.http
      .post<PartyRegistryResponse>(`${this.apiBase}/parties`, {
        name: input.name,
        acronym: input.acronym,
        number: input.number,
        president: input.president,
        ideology: input.ideology,
        handle: input.handle,
        email: input.email,
        password: input.password,
        documentType: 'cnpj',
        documentNumber: input.documentNumber,
      })
      .pipe(
        map(toPartyRegistryEntry),
        tap((entry) => this._parties.update((list) => [...list, entry])),
      );
  }

  assignPolitician(politicianId: string, partyId: string | null): Observable<void> {
    return this.http
      .put<void>(`${this.apiBase}/politician-assignments/${politicianId}`, { partyId })
      .pipe(tap(() => this.directoryService.reloadPoliticians().subscribe()));
  }

  // ---- Platform parameters: countries & states ----
  private readonly _countries = signal<PlatformCountry[]>([]);
  readonly countries = this._countries.asReadonly();

  private readonly _states = signal<PlatformState[]>([]);
  readonly states = this._states.asReadonly();

  reloadCountries(): Observable<PlatformCountry[]> {
    return this.http.get<CountryResponse[]>(`${this.apiBase}/countries`).pipe(
      map((list) => list.map(toPlatformCountry)),
      tap((countries) => {
        this._countries.set(countries);
        forkJoin(countries.map((c) => this.http.get<StateResponse[]>(`${this.apiBase}/countries/${c.id}/states`))).subscribe((results) => {
          this._states.set(results.flat().map(toPlatformState));
        });
      }),
    );
  }

  countryName(id: string): string {
    return this._countries().find((c) => c.id === id)?.name ?? '—';
  }

  statesOf(countryId: string): PlatformState[] {
    return this._states().filter((s) => s.countryId === countryId);
  }

  addCountry(name: string, code: string): void {
    this.http.post<CountryResponse>(`${this.apiBase}/countries`, { name, code: code.toUpperCase() }).subscribe((response) => {
      this._countries.update((list) => [...list, toPlatformCountry(response)]);
    });
  }

  removeCountry(id: string): void {
    this.http.delete<void>(`${this.apiBase}/countries/${id}`).subscribe(() => {
      this._countries.update((list) => list.filter((c) => c.id !== id));
      this._states.update((list) => list.filter((s) => s.countryId !== id));
    });
  }

  addState(name: string, code: string, countryId: string): void {
    this.http.post<StateResponse>(`${this.apiBase}/countries/${countryId}/states`, { name, code: code.toUpperCase() }).subscribe((response) => {
      this._states.update((list) => [...list, toPlatformState(response)]);
    });
  }

  removeState(id: string): void {
    const state = this._states().find((s) => s.id === id);
    if (!state) return;
    this.http.delete<void>(`${this.apiBase}/countries/${state.countryId}/states/${id}`).subscribe(() => {
      this._states.update((list) => list.filter((s) => s.id !== id));
    });
  }

  // ---- Platform parameters: political positions (cargos) ----
  private readonly _politicalPositions = signal<PoliticalPosition[]>([]);
  readonly politicalPositions = this._politicalPositions.asReadonly();

  reloadPoliticalPositions(): Observable<PoliticalPosition[]> {
    return this.http.get<PoliticalPositionResponse[]>(`${this.apiBase}/political-positions`).pipe(
      tap((list) => this._politicalPositions.set(list)),
    );
  }

  addPoliticalPosition(name: string): void {
    this.http.post<PoliticalPositionResponse>(`${this.apiBase}/political-positions`, { name }).subscribe((response) => {
      this._politicalPositions.update((list) => [...list, response]);
    });
  }

  removePoliticalPosition(id: string): void {
    this.http.delete<void>(`${this.apiBase}/political-positions/${id}`).subscribe(() => {
      this._politicalPositions.update((list) => list.filter((p) => p.id !== id));
    });
  }

  // ---- Platform parameters: languages ----
  private readonly _languages = signal<PlatformLanguage[]>([]);
  readonly languages = this._languages.asReadonly();

  reloadLanguages(): Observable<PlatformLanguage[]> {
    return this.http.get<LanguageResponse[]>(`${this.apiBase}/languages`).pipe(
      map((list) => list.map(toPlatformLanguage)),
      tap((languages) => this._languages.set(languages)),
      switchMap((languages) => this.reloadTranslations().pipe(map(() => languages))),
    );
  }

  addLanguage(name: string, code: string): void {
    const id = this.slugify(code || name);
    if (this._languages().some((l) => l.id === id)) return;
    const isDefault = this._languages().length === 0;
    this.http
      .post<LanguageResponse>(`${this.apiBase}/languages`, { id, name, code, isDefault })
      .subscribe(() => this.reloadLanguages().subscribe());
  }

  removeLanguage(id: string): void {
    this.http.delete<void>(`${this.apiBase}/languages/${id}`).subscribe({
      next: () => this.reloadLanguages().subscribe(),
      // Removing the default language is rejected server-side (CannotRemoveDefaultLanguageException) —
      // nothing to reconcile locally since nothing was optimistically changed.
      error: () => undefined,
    });
  }

  setDefaultLanguage(id: string): void {
    this.http.put<LanguageResponse>(`${this.apiBase}/languages/${id}/default`, {}).subscribe(() => this.reloadLanguages().subscribe());
  }

  // ---- Platform parameters: translation tags ----
  private readonly _translations = signal<TranslationEntry[]>([]);
  readonly translations = this._translations.asReadonly();

  reloadTranslations(): Observable<TranslationEntry[]> {
    const languages = this._languages();
    return forkJoin({
      keys: this.http.get<TranslationKeyResponse[]>(`${this.apiBase}/translation-keys`),
      valuesByLanguage: languages.length
        ? forkJoin(languages.map((l) => this.http.get<TranslationValueResponse[]>(`${this.apiBase}/translations`, { params: { languageId: l.id } })))
        : of([] as TranslationValueResponse[][]),
    }).pipe(
      map(({ keys, valuesByLanguage }) => {
        const valuesByKey = new Map<string, Record<string, string>>();
        valuesByLanguage.forEach((values) => {
          for (const v of values) {
            const entry = valuesByKey.get(v.translationKeyId) ?? {};
            entry[v.languageId] = v.value;
            valuesByKey.set(v.translationKeyId, entry);
          }
        });
        return keys.map((k): TranslationEntry => ({ id: k.id, key: k.key, values: valuesByKey.get(k.id) ?? {} }));
      }),
      tap((entries) => this._translations.set(entries)),
    );
  }

  /** Creates a new translation key, seeded with `initialValue` for the default (or first)
   * language — the backend requires a non-blank value to create a key at all. */
  addTranslation(key: string, initialValue: string): void {
    const trimmed = key.trim();
    const value = initialValue.trim();
    if (!trimmed || !value || this._translations().some((t) => t.key === trimmed)) return;
    const language = this._languages().find((l) => l.isDefault) ?? this._languages()[0];
    if (!language) return;
    this.http
      .put(`${this.apiBase}/translations/${trimmed}/${language.id}`, { value })
      .subscribe(() => this.reloadTranslations().subscribe());
  }

  updateTranslationValue(id: string, languageId: string, value: string): void {
    const trimmed = value.trim();
    if (!trimmed) return;
    const entry = this._translations().find((t) => t.id === id);
    if (!entry) return;
    this._translations.update((list) => list.map((t) => (t.id === id ? { ...t, values: { ...t.values, [languageId]: trimmed } } : t)));
    this.http.put(`${this.apiBase}/translations/${entry.key}/${languageId}`, { value: trimmed }).subscribe({
      error: () => this.reloadTranslations().subscribe(),
    });
  }

  removeTranslation(id: string): void {
    this.http.delete<void>(`${this.apiBase}/translation-keys/${id}`).subscribe(() => {
      this._translations.update((list) => list.filter((t) => t.id !== id));
    });
  }

  private slugify(value: string): string {
    return value.trim().toLowerCase().replace(/[^a-z0-9]+/g, '-') || `id-${Date.now()}`;
  }
}
