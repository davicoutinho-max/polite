import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { UiIcon } from '../ui-icon/ui-icon';

export interface StatStripItem {
  readonly icon: string;
  readonly label: string;
  readonly value: string;
  readonly caption?: string;
}

/**
 * A row of stats sitting directly on the page — no surrounding card/box at all, just the numbers
 * with a hairline between them, so a handful of KPIs read as one line rather than a grid of
 * repeated boxes.
 *
 * @example <ui-stat-strip [items]="statItems()" />
 */
@Component({
  selector: 'ui-stat-strip',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon],
  template: `
    <div class="strip">
      @for (item of items(); track item.label) {
        <div class="strip__item">
          <ui-icon [name]="item.icon" [size]="20" />
          <div class="strip__value">{{ item.value }}</div>
          <div class="strip__label">{{ item.label }}</div>
          @if (item.caption; as c) {
            <div class="strip__caption">{{ c }}</div>
          }
        </div>
      }
    </div>
  `,
  styles: `
    :host { display: block; }
    .strip {
      display: flex;
      flex-wrap: wrap;
    }
    .strip__item {
      flex: 1 1 150px;
      min-width: 0;
      padding: var(--cp-space-sm) var(--cp-space-md);
      display: flex;
      flex-direction: column;
      gap: 2px;
      color: var(--cp-secondary);
    }
    .strip__item:first-child {
      padding-left: 0;
    }
    .strip__value {
      font-size: 24px;
      line-height: 32px;
      font-weight: 700;
      color: var(--cp-on-surface);
    }
    .strip__label {
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 0.05em;
      text-transform: uppercase;
      color: var(--cp-on-surface-variant);
    }
    .strip__caption { font-size: 12px; color: var(--cp-on-surface-variant); }

    @media (min-width: 640px) {
      .strip__item:not(:first-child) { border-left: 1px solid var(--cp-outline-variant); }
    }
    @media (max-width: 639px) {
      .strip__item {
        flex: 1 1 50%;
        padding-left: var(--cp-space-md);
        border-bottom: 1px solid var(--cp-outline-variant);
      }
      .strip__item:nth-child(odd) { padding-left: 0; }
      .strip__item:nth-last-child(-n+2) { border-bottom: none; }
    }
  `,
})
export class UiStatStrip {
  readonly items = input.required<StatStripItem[]>();
}
