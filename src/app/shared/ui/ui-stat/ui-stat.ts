import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { UiIcon } from '../ui-icon/ui-icon';

/**
 * Generic stat / KPI tile.
 *
 * @example <ui-stat icon="payments" label="Salary" value="R$ 41.650" caption="Gross monthly" />
 */
@Component({
  selector: 'ui-stat',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon],
  template: `
    <div class="stat">
      <div class="stat__icon">
        <ui-icon [name]="icon()" [size]="22" />
      </div>
      <div class="stat__body">
        <div class="stat__label">{{ label() }}</div>
        <div class="stat__value">{{ value() }}</div>
        @if (caption(); as c) {
          <div class="stat__caption">{{ c }}</div>
        }
      </div>
    </div>
  `,
  styles: `
    :host { display: block; min-width: 0; }
    .stat {
      display: flex;
      gap: var(--cp-space-md);
      align-items: flex-start;
      background: var(--cp-surface-container-lowest);
      border: 1px solid var(--cp-outline-variant);
      border-radius: var(--cp-radius-lg);
      padding: var(--cp-space-md);
      height: 100%;
      min-width: 0;
    }
    .stat__icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      border-radius: var(--cp-radius);
      background: var(--cp-surface-container-high);
      color: var(--cp-secondary);
      flex-shrink: 0;
    }
    .stat__body { min-width: 0; }
    .stat__label {
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 0.05em;
      text-transform: uppercase;
      color: var(--cp-on-surface-variant);
    }
    .stat__value {
      font-size: 24px;
      line-height: 32px;
      font-weight: 700;
      color: var(--cp-on-surface);
    }
    .stat__caption { font-size: 12px; color: var(--cp-on-surface-variant); }
  `,
})
export class UiStat {
  readonly icon = input.required<string>();
  readonly label = input.required<string>();
  readonly value = input.required<string>();
  readonly caption = input<string>();
}
