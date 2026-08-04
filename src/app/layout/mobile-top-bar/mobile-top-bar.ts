import { ChangeDetectionStrategy, Component } from '@angular/core';
import { UiIconButton } from '../../shared/ui/ui-icon-button/ui-icon-button';

/** Sticky top app bar shown only on mobile. */
@Component({
  selector: 'app-mobile-top-bar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIconButton],
  template: `
    <header class="top-bar">
      <h1 class="top-bar__brand">
        <img src="logo.svg" alt="" width="26" height="26" />
        <span>IQORUM</span>
      </h1>
      <div class="top-bar__actions">
        <ui-icon-button icon="search" ariaLabel="Search" />
        <ui-icon-button icon="notifications" ariaLabel="Notifications" />
      </div>
    </header>
  `,
  styles: `
    :host { display: block; }
    @media (min-width: 768px) { :host { display: none; } }

    .top-bar {
      position: sticky;
      top: 0;
      z-index: 50;
      display: flex;
      justify-content: space-between;
      align-items: center;
      height: 64px;
      padding: 0 var(--cp-margin-mobile);
      background: var(--cp-surface-container-lowest);
      border-bottom: 1px solid var(--cp-outline-variant);
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
    }
    .top-bar__brand {
      display: flex;
      align-items: center;
      gap: var(--cp-space-xs);
      margin: 0;
      font-size: 24px;
      font-weight: 900;
      letter-spacing: 0.02em;

      img { border-radius: var(--cp-radius-md); box-shadow: 0 4px 14px rgba(21, 60, 106, 0.4); }

      span {
        background: linear-gradient(90deg, #4f8ef0, #52dc9a);
        -webkit-background-clip: text;
        background-clip: text;
        color: transparent;
      }
    }
    .top-bar__actions {
      display: flex;
      gap: var(--cp-space-sm);
    }
  `,
})
export class MobileTopBar {}
