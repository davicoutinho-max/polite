import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { LegislativeItem } from '../../../../core/models';
import { UiEmpty } from '../../../../shared/ui/ui-empty/ui-empty';
import { UiSection } from '../../../../shared/ui/ui-section/ui-section';
import { UiTag } from '../../../../shared/ui/ui-tag/ui-tag';

/** Reusable titled list of legislative items (bills, laws, PECs, CPIs). */
@Component({
  selector: 'app-legislative-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiSection, UiTag, UiEmpty],
  template: `
    <ui-section [title]="title()" [icon]="icon()">
      @if (items().length) {
        <ul class="items">
          @for (item of items(); track item.id) {
            <li class="item">
              <div class="item__head">
                <span class="item__ref">{{ item.reference }}</span>
                <ui-tag [label]="item.status.label" [severity]="item.status.severity" />
              </div>
              <div class="item__title">{{ item.title }}</div>
              <p class="item__summary">{{ item.summary }}</p>
              <div class="item__date">{{ item.date }}</div>
            </li>
          }
        </ul>
      } @else {
        <ui-empty icon="draft" [title]="emptyTitle()" />
      }
    </ui-section>
  `,
  styles: `
    :host { display: block; }
    .items { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: var(--cp-space-md); }
    .item {
      border: 1px solid var(--cp-outline-variant);
      border-radius: var(--cp-radius);
      background: var(--cp-surface);
      padding: var(--cp-space-md);
    }
    .item__head { display: flex; justify-content: space-between; align-items: flex-start; gap: var(--cp-space-sm); }
    .item__ref { font-size: 12px; font-weight: 600; letter-spacing: 0.05em; color: var(--cp-secondary); }
    .item__title { font-size: 16px; font-weight: 600; color: var(--cp-on-surface); margin-top: var(--cp-space-xs); }
    .item__summary { margin: var(--cp-space-xs) 0 0; font-size: 14px; line-height: 20px; color: var(--cp-on-surface-variant); }
    .item__date { margin-top: var(--cp-space-sm); font-size: 12px; color: var(--cp-on-surface-variant); }
  `,
})
export class LegislativeList {
  readonly title = input.required<string>();
  readonly icon = input('gavel');
  readonly items = input.required<readonly LegislativeItem[]>();
  readonly emptyTitle = input('Nothing here yet');
}
