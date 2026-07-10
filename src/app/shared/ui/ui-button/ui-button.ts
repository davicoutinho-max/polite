import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Ripple } from 'primeng/ripple';
import { UiIcon } from '../ui-icon/ui-icon';

export type UiButtonVariant = 'filled' | 'tonal' | 'outlined' | 'text';

/**
 * Generic action button aligned to the design system. Fully driven by inputs so
 * callers only pass parameters.
 *
 * @example
 * <ui-button label="Post" variant="filled" />
 * <ui-button label="Follow" icon="person_add" variant="outlined" [block]="true" />
 */
@Component({
  selector: 'ui-button',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon, Ripple],
  template: `
    <button
      pRipple
      [type]="type()"
      class="ui-button"
      [class.ui-button--filled]="variant() === 'filled'"
      [class.ui-button--tonal]="variant() === 'tonal'"
      [class.ui-button--outlined]="variant() === 'outlined'"
      [class.ui-button--text]="variant() === 'text'"
      [class.ui-button--block]="block()"
      [disabled]="disabled()"
    >
      @if (icon(); as ic) {
        <ui-icon [name]="ic" [size]="18" />
      }
      <span>{{ label() }}</span>
    </button>
  `,
  styles: `
    :host { display: inline-flex; }
    :host(.ui-button--host-block) { display: block; }
    .ui-button {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: var(--cp-space-sm);
      padding: var(--cp-space-sm) var(--cp-space-lg);
      border-radius: var(--cp-radius);
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 0.05em;
      line-height: 16px;
      border: 1px solid transparent;
      cursor: pointer;
      transition: opacity 0.2s ease, background 0.2s ease, color 0.2s ease;
    }
    .ui-button--block { width: 100%; }
    .ui-button:disabled { opacity: 0.5; cursor: not-allowed; }
    .ui-button:focus-visible { outline: 2px solid var(--cp-secondary); outline-offset: 2px; }

    .ui-button--filled {
      background: var(--cp-primary-container);
      color: var(--cp-on-primary);
    }
    .ui-button--filled:hover:not(:disabled) { opacity: 0.9; }

    .ui-button--tonal {
      background: var(--cp-secondary-container);
      color: var(--cp-on-secondary-container);
    }
    .ui-button--tonal:hover:not(:disabled) { opacity: 0.9; }

    .ui-button--outlined {
      border-color: var(--cp-secondary);
      color: var(--cp-secondary);
      background: transparent;
    }
    .ui-button--outlined:hover:not(:disabled) { background: var(--cp-surface-container-low); }

    .ui-button--text {
      background: transparent;
      color: var(--cp-secondary);
      padding-inline: var(--cp-space-sm);
    }
    .ui-button--text:hover:not(:disabled) { text-decoration: underline; }
  `,
  host: {
    '[class.ui-button--host-block]': 'block()',
  },
})
export class UiButton {
  readonly label = input.required<string>();
  readonly icon = input<string>();
  readonly variant = input<UiButtonVariant>('filled');
  readonly block = input(false);
  readonly disabled = input(false);
  readonly type = input<'button' | 'submit'>('button');
}
