import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Generic surface card matching the design system elevation & radius.
 * Content is projected; enable `hover` for the interactive lift effect used by
 * feed posts and bill cards.
 *
 * @example
 * <ui-card hover padding="lg">…</ui-card>
 */
@Component({
  selector: 'ui-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<ng-content />`,
  host: {
    class: 'ui-card',
    '[class.ui-card--hover]': 'hover()',
    '[class.ui-card--pad-none]': "padding() === 'none'",
    '[class.ui-card--pad-md]': "padding() === 'md'",
    '[class.ui-card--pad-lg]': "padding() === 'lg'",
  },
  styles: `
    :host {
      display: block;
      background: var(--cp-surface-container-lowest);
      border: 1px solid var(--cp-outline-variant);
      border-radius: var(--cp-radius-lg);
      box-shadow: var(--cp-shadow-card);
      transition: box-shadow 0.2s ease, transform 0.2s ease;
    }
    :host(.ui-card--pad-none) { padding: 0; }
    :host(.ui-card--pad-md)   { padding: var(--cp-space-md); }
    :host(.ui-card--pad-lg)   { padding: var(--cp-space-lg); }
    :host(.ui-card--hover:hover) {
      box-shadow: var(--cp-shadow-hover);
      transform: translateY(-1px);
    }
  `,
})
export class UiCard {
  readonly hover = input(false);
  readonly padding = input<'none' | 'md' | 'lg'>('lg');
}
