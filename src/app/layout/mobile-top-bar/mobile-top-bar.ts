import { ChangeDetectionStrategy, Component } from '@angular/core';
import { UiIconButton } from '../../shared/ui/ui-icon-button/ui-icon-button';

/** Sticky top app bar shown only on mobile. */
@Component({
  selector: 'app-mobile-top-bar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIconButton],
  template: `
    <header class="top-bar">
      <h1 class="top-bar__brand">CivicPulse</h1>
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
      margin: 0;
      font-size: 24px;
      font-weight: 700;
      color: var(--cp-primary);
    }
    .top-bar__actions {
      display: flex;
      gap: var(--cp-space-sm);
    }
  `,
})
export class MobileTopBar {}
