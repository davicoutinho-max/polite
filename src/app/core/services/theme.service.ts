import { computed, effect, Injectable, signal } from '@angular/core';

export type ThemeMode = 'light' | 'dark';

const STORAGE_KEY = 'cp-theme';
const DARK_CLASS = 'cp-dark';

/**
 * App theme (light/dark). Initialises from a saved preference, falling back to
 * the OS `prefers-color-scheme`, and reflects the choice onto <html>.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly _mode = signal<ThemeMode>(this.resolveInitial());

  readonly mode = this._mode.asReadonly();
  readonly isDark = computed(() => this._mode() === 'dark');

  constructor() {
    // Keep the <html> class and the stored preference in sync with the signal.
    effect(() => {
      const dark = this._mode() === 'dark';
      const root = document.documentElement;
      root.classList.toggle(DARK_CLASS, dark);
      try {
        localStorage.setItem(STORAGE_KEY, this._mode());
      } catch {
        /* storage unavailable — ignore */
      }
    });
  }

  toggle(): void {
    this._mode.update((m) => (m === 'dark' ? 'light' : 'dark'));
  }

  set(mode: ThemeMode): void {
    this._mode.set(mode);
  }

  private resolveInitial(): ThemeMode {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved === 'light' || saved === 'dark') {
        return saved;
      }
    } catch {
      /* ignore */
    }
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
}
