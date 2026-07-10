import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NavItem } from '../../../core/models';
import { UiIcon } from '../ui-icon/ui-icon';
import { TranslatePipe } from '../../pipes/translate.pipe';

/**
 * Generic navigation entry driven by a {@link NavItem}. Renders differently for
 * the desktop sidebar vs. the mobile bottom bar via `layout`, and highlights the
 * active route automatically. Pass `badge` to show an unread-style counter.
 *
 * @example <ui-nav-item [item]="item" layout="sidebar" [badge]="unread()" />
 */
@Component({
  selector: 'ui-nav-item',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, UiIcon, TranslatePipe],
  template: `
    <a
      class="nav-item"
      [class.nav-item--bottom]="layout() === 'bottom'"
      [routerLink]="item().route"
      routerLinkActive="nav-item--active"
      [routerLinkActiveOptions]="{ exact: false }"
      #rla="routerLinkActive"
    >
      <span class="nav-item__icon">
        <ui-icon [name]="item().icon" [fill]="rla.isActive" [size]="layout() === 'bottom' ? 24 : 22" />
        @if (badge() > 0) {
          <span class="nav-item__badge">{{ badge() > 9 ? '9+' : badge() }}</span>
        }
      </span>
      <span class="nav-item__label">{{ item().key | translate: item().label }}</span>
    </a>
  `,
  styles: `
    :host { display: block; }
    .nav-item {
      display: flex;
      align-items: center;
      gap: var(--cp-space-md);
      padding: 10px var(--cp-space-md);
      border-radius: var(--cp-radius-lg);
      color: var(--cp-on-surface-variant);
      font-size: 14px;
      font-weight: 600;
      letter-spacing: 0.01em;
      transition: background 0.15s ease, color 0.15s ease;
    }
    .nav-item:hover { background: var(--cp-surface-container-high); color: var(--cp-on-surface); }
    .nav-item--active {
      background: var(--cp-secondary-container);
      color: var(--cp-on-secondary-container);
      font-weight: 700;
    }
    .nav-item__icon { position: relative; display: inline-flex; }
    .nav-item__badge {
      position: absolute;
      top: -6px;
      right: -8px;
      min-width: 16px;
      height: 16px;
      padding: 0 4px;
      border-radius: var(--cp-radius-full);
      background: var(--cp-error);
      color: var(--cp-on-error);
      font-size: 10px;
      font-weight: 700;
      line-height: 16px;
      text-align: center;
    }

    /* Mobile bottom bar */
    .nav-item--bottom {
      flex-direction: column;
      gap: 2px;
      justify-content: center;
      padding: var(--cp-space-xs);
      border-radius: 0;
      background: transparent;
      font-size: 10px;
    }
    .nav-item--bottom:hover { background: transparent; color: var(--cp-secondary); }
    .nav-item--bottom .nav-item__label { font-size: 10px; letter-spacing: 0; }
    .nav-item--bottom.nav-item--active { background: transparent; color: var(--cp-secondary); }
  `,
})
export class UiNavItem {
  readonly item = input.required<NavItem>();
  readonly layout = input<'sidebar' | 'bottom'>('sidebar');
  readonly badge = input(0);
}
