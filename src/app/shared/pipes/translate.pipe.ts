import { inject, Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '../../core/services/translate.service';

/**
 * Looks up `key` in the platform's translation registry for the current
 * language, falling back to `fallback` (the English copy already in the
 * template) when no translated value exists yet. Impure so it re-evaluates
 * whenever the selected language changes.
 * @example {{ 'nav.feed' | translate:'Feed' }}
 */
@Pipe({ name: 'translate', pure: false })
export class TranslatePipe implements PipeTransform {
  private readonly translate = inject(TranslateService);

  transform(key: string, fallback: string): string {
    return this.translate.t(key, fallback);
  }
}
