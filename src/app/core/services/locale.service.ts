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

  // The interface's literal template text is authored in English, so that's
  // the language state that actually matches what's on screen by default.
  private readonly _currentId = signal('en-us');

  readonly languages = this.platform.languages;
  readonly current = computed(
    () => this.languages().find((l) => l.id === this._currentId()) ?? this.languages()[0],
  );

  setCurrent(id: string): void {
    this._currentId.set(id);
  }
}
