import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

function toCss(value: string | number): string {
  return typeof value === 'number' ? `${value}px` : value;
}

/**
 * Generic content-shaped loading placeholder — compose several of these directly in a page's own
 * `@if (loading()) { ... }` branch to approximate the real layout (same number of "lines"/circles/
 * rects, similar gaps) rather than a spinner or "Loading…" text.
 *
 * @example
 * <ui-skeleton shape="circle" [width]="48" />
 * <ui-skeleton width="140px" height="16px" />
 * <ui-skeleton shape="rect" width="100%" height="80px" />
 */
@Component({
  selector: 'ui-skeleton',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="ui-skeleton__shimmer"></span>`,
  host: {
    class: 'ui-skeleton',
    '[class.ui-skeleton--circle]': "shape() === 'circle'",
    '[class.ui-skeleton--text]': "shape() === 'text'",
    '[class.ui-skeleton--rect]': "shape() === 'rect'",
    '[style.width]': 'cssWidth()',
    '[style.height]': 'cssHeight()',
    '[attr.aria-hidden]': 'true',
  },
  styles: `
    :host {
      display: block;
      position: relative;
      overflow: hidden;
      background: var(--cp-surface-container-highest);
      flex-shrink: 0;
    }
    :host(.ui-skeleton--text) { border-radius: var(--cp-radius); }
    :host(.ui-skeleton--rect) { border-radius: var(--cp-radius-lg); }
    :host(.ui-skeleton--circle) { border-radius: var(--cp-radius-full); }

    .ui-skeleton__shimmer {
      position: absolute;
      inset: 0;
      background: linear-gradient(100deg, transparent 30%, var(--cp-surface-container-low) 50%, transparent 70%);
      background-size: 200% 100%;
      background-position: 150% 0;
    }
    @media (prefers-reduced-motion: no-preference) {
      .ui-skeleton__shimmer {
        animation: ui-skeleton-shimmer 1.6s ease-in-out infinite;
      }
    }
    @keyframes ui-skeleton-shimmer {
      from { background-position: 150% 0; }
      to { background-position: -50% 0; }
    }
  `,
})
export class UiSkeleton {
  readonly shape = input<'text' | 'circle' | 'rect'>('text');
  readonly width = input<string | number>('100%');
  readonly height = input<string | number>();

  protected readonly cssWidth = computed(() => toCss(this.width()));
  protected readonly cssHeight = computed(() => {
    const height = this.height();
    if (height !== undefined) {
      return toCss(height);
    }
    if (this.shape() === 'circle') {
      return toCss(this.width());
    }
    if (this.shape() === 'text') {
      return '0.85em';
    }
    return '80px';
  });
}
