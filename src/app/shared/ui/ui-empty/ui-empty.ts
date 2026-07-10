import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { UiIcon } from '../ui-icon/ui-icon';

/** Generic empty-state placeholder. */
@Component({
  selector: 'ui-empty',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon],
  template: `
    <div class="empty">
      <ui-icon [name]="icon()" [size]="40" />
      <p class="empty__title">{{ title() }}</p>
      @if (description(); as d) {
        <p class="empty__desc">{{ d }}</p>
      }
    </div>
  `,
  styles: `
    :host { display: block; }
    .empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: var(--cp-space-sm);
      padding: var(--cp-space-xl);
      text-align: center;
      color: var(--cp-on-surface-variant);
    }
    .empty__title { margin: 0; font-weight: 600; color: var(--cp-on-surface); }
    .empty__desc { margin: 0; font-size: 14px; }
  `,
  host: {
    // The static `title` input would otherwise also be reflected as the
    // native HTML `title` attribute, triggering a stray browser tooltip.
    '[attr.title]': 'null',
  },
})
export class UiEmpty {
  readonly icon = input('inbox');
  readonly title = input.required<string>();
  readonly description = input<string>();
}
