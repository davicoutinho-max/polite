import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export interface BarDatum {
  readonly label: string;
  readonly value: number;
  /** Optional preformatted value (e.g. "R$ 1.2k"); defaults to the number. */
  readonly display?: string;
}

/**
 * Generic single-hue horizontal bar chart (magnitude by category).
 * Marks are thin with a 4px rounded data-end anchored to the baseline; text uses
 * ink tokens, the mark carries the single sequential hue.
 *
 * @example <ui-bar-chart [data]="items" />
 */
@Component({
  selector: 'ui-bar-chart',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ul class="bars">
      @for (item of data(); track item.label) {
        <li class="bar">
          <div class="bar__head">
            <span class="bar__label">{{ item.label }}</span>
            <span class="bar__value">{{ item.display ?? item.value }}</span>
          </div>
          <div class="bar__track">
            <div
              class="bar__fill"
              [style.width.%]="pct(item.value)"
              [attr.title]="item.label + ': ' + (item.display ?? item.value)"
            ></div>
          </div>
        </li>
      }
    </ul>
  `,
  styles: `
    :host { display: block; }
    .bars { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: var(--cp-space-md); }
    .bar__head { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: var(--cp-space-xs); }
    .bar__label { font-size: 14px; color: var(--cp-on-surface); font-weight: 600; }
    .bar__value { font-size: 13px; color: var(--cp-on-surface-variant); font-weight: 600; }
    .bar__track { height: 12px; background: var(--cp-surface-container-high); border-radius: var(--cp-radius-full); overflow: hidden; }
    .bar__fill {
      height: 100%;
      min-width: 4px;
      background: var(--cp-secondary);
      border-radius: 0 var(--cp-radius) var(--cp-radius) 0;
      transition: width 0.5s ease;
    }
  `,
})
export class UiBarChart {
  readonly data = input.required<readonly BarDatum[]>();

  private readonly max = computed(() => Math.max(1, ...this.data().map((d) => d.value)));

  protected pct(value: number): number {
    return Math.round((value / this.max()) * 100);
  }
}
