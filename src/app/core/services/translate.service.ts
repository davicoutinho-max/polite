import { inject, Injectable } from '@angular/core';
import { PlatformService } from './platform.service';
import { LocaleService } from './locale.service';

/**
 * Resolves a translation key against the platform's translation-tag registry
 * for the currently selected language, falling back to the caller-supplied
 * (English) text when no entry or no value exists yet for that language.
 */
@Injectable({ providedIn: 'root' })
export class TranslateService {
  private readonly platform = inject(PlatformService);
  private readonly locale = inject(LocaleService);

  t(key: string, fallback: string, params?: Record<string, string | number>): string {
    const currentId = this.locale.current()?.id;
    const entry = this.platform.translations().find((e) => e.key === key);
    const value = currentId ? entry?.values[currentId] : undefined;
    return this.interpolate(value ? value : fallback, params);
  }

  private interpolate(text: string, params?: Record<string, string | number>): string {
    if (!params) {
      return text;
    }
    return Object.entries(params).reduce((result, [name, value]) => result.replaceAll(`{{${name}}}`, String(value)), text);
  }
}
