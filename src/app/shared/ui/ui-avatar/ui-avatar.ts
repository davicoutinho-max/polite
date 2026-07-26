import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/** Neutral silhouette shown whenever a real photo isn't available (e.g. politicians synced from
 * government open-data sources — TSE never publishes photos, and Câmara/Senado sometimes omit
 * one) — never a stock photo standing in for a specific, unrelated person. */
const FALLBACK_AVATAR =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 40 40'%3E%3Crect width='40' height='40' fill='%23c7ccd1'/%3E%3Ccircle cx='20' cy='15' r='7' fill='%23fff'/%3E%3Cpath d='M6 38c0-8 6-13 14-13s14 5 14 13z' fill='%23fff'/%3E%3C/svg%3E";

/**
 * Generic circular avatar with configurable size and optional ring.
 *
 * @example <ui-avatar [src]="user.avatarUrl" [alt]="user.name" [size]="48" ring />
 */
@Component({
  selector: 'ui-avatar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <img
      class="ui-avatar__img"
      [src]="displaySrc()"
      [alt]="alt()"
      loading="lazy"
      referrerpolicy="no-referrer"
    />
  `,
  host: {
    class: 'ui-avatar',
    '[class.ui-avatar--ring]': 'ring()',
    '[style.width.px]': 'size()',
    '[style.height.px]': 'size()',
  },
  styles: `
    :host {
      display: inline-block;
      border-radius: var(--cp-radius-full);
      overflow: hidden;
      flex-shrink: 0;
      background: var(--cp-surface-variant);
    }
    :host(.ui-avatar--ring) {
      border: 2px solid var(--cp-surface-container-lowest);
      box-shadow: 0 0 0 1px var(--cp-outline-variant);
    }
    .ui-avatar__img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  `,
})
export class UiAvatar {
  readonly src = input.required<string | null | undefined>();
  readonly alt = input('');
  readonly size = input(40);
  readonly ring = input(false);

  readonly displaySrc = computed(() => this.src() || FALLBACK_AVATAR);
}
