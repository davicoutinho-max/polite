import { ChangeDetectionStrategy, Component, input } from '@angular/core';

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
      [src]="src()"
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
  readonly src = input.required<string>();
  readonly alt = input('');
  readonly size = input(40);
  readonly ring = input(false);
}
