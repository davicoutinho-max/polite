import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { UiIcon } from '../ui-icon/ui-icon';

/**
 * Generic circular, ghost icon button. Accessible by default via `ariaLabel`.
 *
 * @example <ui-icon-button icon="more_horiz" ariaLabel="More options" (click)="…" />
 */
@Component({
  selector: 'ui-icon-button',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon],
  template: `
    <button type="button" class="ui-icon-button" [attr.aria-label]="ariaLabel()">
      <ui-icon [name]="icon()" [size]="iconSize()" [fill]="fill()" />
    </button>
  `,
  styles: `
    :host { display: inline-flex; }
    .ui-icon-button {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: var(--cp-space-sm);
      border: none;
      background: transparent;
      color: var(--cp-secondary);
      border-radius: var(--cp-radius-full);
      cursor: pointer;
      transition: background 0.2s ease, color 0.2s ease, opacity 0.2s ease;
    }
    .ui-icon-button:hover { background: var(--cp-surface-container-low); }
    .ui-icon-button:active { opacity: 0.8; }
    .ui-icon-button:focus-visible {
      outline: 2px solid var(--cp-secondary);
      outline-offset: 2px;
    }
  `,
})
export class UiIconButton {
  readonly icon = input.required<string>();
  readonly ariaLabel = input.required<string>();
  readonly iconSize = input(22);
  readonly fill = input(false);
}
