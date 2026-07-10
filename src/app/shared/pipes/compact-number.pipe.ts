import { Pipe, PipeTransform } from '@angular/core';

/**
 * Formats large counts compactly: 1200 → "1.2k", 45200 → "45.2k".
 * @example {{ post.metrics.likes | compactNumber }}
 */
@Pipe({ name: 'compactNumber' })
export class CompactNumberPipe implements PipeTransform {
  transform(value: number | null | undefined): string {
    const n = value ?? 0;
    if (n < 1000) {
      return `${n}`;
    }
    if (n < 1_000_000) {
      return `${this.trim(n / 1000)}k`;
    }
    return `${this.trim(n / 1_000_000)}M`;
  }

  private trim(v: number): string {
    return v.toFixed(1).replace(/\.0$/, '');
  }
}
