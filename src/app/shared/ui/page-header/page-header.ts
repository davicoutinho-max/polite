import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { UiIcon } from '../ui-icon/ui-icon';

/**
 * Generic page header (title + subtitle + optional icon and trailing action).
 * Project a `[slot=action]` element for the trailing control.
 */
@Component({
  selector: 'app-page-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon],
  template: `
    <header class="page-header">
      <div class="page-header__lead">
        @if (icon(); as ic) {
          <div class="page-header__icon">
            <ui-icon [name]="ic" [size]="24" [fill]="true" />
          </div>
        }
        <div>
          <h1 class="page-header__title">{{ title() }}</h1>
          @if (subtitle(); as s) {
            <p class="page-header__subtitle">{{ s }}</p>
          }
        </div>
      </div>
      <div class="page-header__action">
        <ng-content select="[slot=action]" />
      </div>
    </header>
  `,
  styles: `
    :host { display: block; }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: var(--cp-space-md);
      flex-wrap: wrap;
      margin-bottom: var(--cp-space-lg);
    }
    .page-header__lead { display: flex; align-items: center; gap: var(--cp-space-md); }
    .page-header__icon {
      display: inline-flex; align-items: center; justify-content: center;
      width: 48px; height: 48px; border-radius: var(--cp-radius-lg);
      background: var(--cp-secondary-container); color: var(--cp-on-secondary-container);
      flex-shrink: 0;
    }
    .page-header__title {
      margin: 0; font-size: 32px; line-height: 40px; font-weight: 700;
      letter-spacing: -0.01em; color: var(--cp-on-surface);
    }
    .page-header__subtitle { margin: 2px 0 0; font-size: 14px; color: var(--cp-on-surface-variant); }
    @media (max-width: 640px) {
      .page-header__title { font-size: 24px; line-height: 32px; }
    }
  `,
  host: {
    // The static `title` input would otherwise also be reflected as the
    // native HTML `title` attribute, triggering a stray browser tooltip.
    '[attr.title]': 'null',
  },
})
export class PageHeader {
  readonly title = input.required<string>();
  readonly subtitle = input<string>();
  readonly icon = input<string>();
}
