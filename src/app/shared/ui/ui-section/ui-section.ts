import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { UiIcon } from '../ui-icon/ui-icon';

/**
 * Generic titled section card. Header shows an optional icon + title; project a
 * `[slot=action]` element for a trailing action, and default content for the body.
 *
 * @example
 * <ui-section title="Trending Topics" icon="trending_up">…</ui-section>
 */
@Component({
  selector: 'ui-section',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon],
  template: `
    <div class="section" [class.section--plain]="plain()">
      <header class="section__head">
        <div class="section__title">
          @if (icon(); as ic) {
            <ui-icon [name]="ic" [size]="20" />
          }
          <h3>{{ title() }}</h3>
        </div>
        <div class="section__action">
          <ng-content select="[slot=action]" />
        </div>
      </header>
      <div class="section__body">
        <ng-content />
      </div>
    </div>
  `,
  styles: `
    :host { display: block; }
    .section {
      background: var(--cp-surface-container-lowest);
      border: 1px solid var(--cp-outline-variant);
      border-radius: var(--cp-radius-lg);
      box-shadow: var(--cp-shadow-card);
      padding: var(--cp-space-lg);
    }
    .section--plain {
      background: transparent;
      border: none;
      box-shadow: none;
      padding: 0;
    }
    .section__head {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: var(--cp-space-md);
      margin-bottom: var(--cp-space-md);
      padding-bottom: var(--cp-space-sm);
      border-bottom: 1px solid var(--cp-outline-variant);
    }
    .section--plain .section__head { border-bottom: none; padding-bottom: 0; }
    .section__title {
      display: flex;
      align-items: center;
      gap: var(--cp-space-sm);
      color: var(--cp-secondary);
    }
    .section__title h3 {
      margin: 0;
      font-size: 20px;
      line-height: 28px;
      font-weight: 600;
      color: var(--cp-on-surface);
    }
  `,
  host: {
    // The static `title` input would otherwise also be reflected as the
    // native HTML `title` attribute, triggering a stray browser tooltip.
    '[attr.title]': 'null',
  },
})
export class UiSection {
  readonly title = input.required<string>();
  readonly icon = input<string>();
  readonly plain = input(false);
}
