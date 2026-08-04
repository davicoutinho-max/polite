import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { UiIcon } from '../ui-icon/ui-icon';
import { UiExpandableText } from '../ui-expandable-text/ui-expandable-text';

export interface DataListItem {
  readonly icon?: string;
  readonly label: string;
  readonly value: string;
}

/**
 * Generic definition list (label → value rows). Ideal for profile dossiers — some values (a
 * politician's education/profession free-text, say) can run to whole paragraphs, so each value
 * clamps behind a "show more" toggle rather than pushing the rest of the page down.
 *
 * @example <ui-data-list [items]="[{ label: 'Profession', value: 'Lawyer' }]" />
 */
@Component({
  selector: 'ui-data-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon, UiExpandableText],
  template: `
    <dl class="data-list" [class.data-list--cols]="columns()">
      @for (item of items(); track item.label) {
        <div class="data-list__row">
          <dt class="data-list__label">
            @if (item.icon; as ic) {
              <ui-icon [name]="ic" [size]="16" />
            }
            {{ item.label }}
          </dt>
          <dd class="data-list__value">
            <ui-expandable-text [text]="item.value" [maxLines]="3" [moreLabel]="moreLabel()" [lessLabel]="lessLabel()" />
          </dd>
        </div>
      }
    </dl>
  `,
  styles: `
    :host { display: block; }
    .data-list { margin: 0; display: flex; flex-direction: column; gap: var(--cp-space-md); }
    .data-list--cols {
      display: grid;
      grid-template-columns: 1fr;
      gap: var(--cp-space-md) var(--cp-space-lg);
    }
    @media (min-width: 640px) {
      .data-list--cols { grid-template-columns: 1fr 1fr; }
    }
    .data-list__row { display: flex; flex-direction: column; gap: 2px; }
    .data-list__label {
      display: inline-flex;
      align-items: center;
      gap: var(--cp-space-xs);
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 0.05em;
      text-transform: uppercase;
      color: var(--cp-on-surface-variant);
    }
    .data-list__value { margin: 0; font-size: 16px; color: var(--cp-on-surface); font-weight: 600; overflow-wrap: anywhere; }
  `,
})
export class UiDataList {
  readonly items = input.required<readonly DataListItem[]>();
  readonly columns = input(false);
  readonly moreLabel = input('Show more');
  readonly lessLabel = input('Show less');
}
