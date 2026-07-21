import { computed, Injectable, inject, signal } from '@angular/core';
import { PlatformService } from './platform.service';

/**
 * The language the signed-in user is currently viewing the platform in.
 * Available languages come from the platform-admin registry; the selection
 * itself is per-session UI state, independent from the platform's default.
 */
@Injectable({ providedIn: 'root' })
export class LocaleService {
  private readonly platform = inject(PlatformService);

  // No explicit user choice yet — `current` falls back to whichever language the platform
  // registry itself marks as default, rather than assuming a specific id like 'en-us' exists.
  // A hardcoded id silently mismatching the registry is exactly how this stayed broken before:
  // the registry only ever held stray test rows, none named 'en-us', so `current` fell back to
  // whatever row happened to load first instead of a real language.
  private readonly _currentId = signal<string | null>(null);

  readonly languages = this.platform.languages;
  readonly current = computed(() => {
    const chosen = this._currentId();
    const list = this.languages();
    return (chosen ? list.find((l) => l.id === chosen) : undefined) ?? list.find((l) => l.isDefault) ?? list.at(0);
  });

  setCurrent(id: string): void {
    this._currentId.set(id);
  }
}
