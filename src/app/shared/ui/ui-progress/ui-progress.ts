import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/**
 * Generic slim progress bar used for legislative / funding progress.
 *
 * @example <ui-progress [value]="45" color="secondary" />
 */
@Component({
  selector: 'ui-progress',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="ui-progress__track"
      role="progressbar"
      [attr.aria-valuenow]="value()"
      aria-valuemin="0"
      aria-valuemax="100"
    >
      <div class="ui-progress__fill" [class]="'ui-progress__fill--' + color()" [style.width.%]="clamped()"></div>
    </div>
  `,
  styles: `
    :host { display: block; }
    .ui-progress__track {
      height: 8px;
      width: 100%;
      background: var(--cp-surface-container-highest);
      border-radius: var(--cp-radius-full);
      overflow: hidden;
    }
    .ui-progress__fill {
      height: 100%;
      border-radius: var(--cp-radius-full);
      transition: width 0.4s ease;
    }
    .ui-progress__fill--secondary { background: var(--cp-secondary); }
    .ui-progress__fill--tertiary  { background: var(--cp-tertiary-fixed-dim); }
  `,
})
export class UiProgress {
  readonly value = input(0);
  readonly color = input<'secondary' | 'tertiary'>('secondary');

  protected readonly clamped = computed(() => Math.max(0, Math.min(100, this.value())));
}
