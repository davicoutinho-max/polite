import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { NavigationService } from '../../core/services/navigation.service';
import { UiNavItem } from '../../shared/ui/ui-nav-item/ui-nav-item';

/** Fixed bottom navigation shown only on mobile. */
@Component({
  selector: 'app-mobile-bottom-nav',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiNavItem],
  template: `
    <nav class="bottom-nav">
      @for (item of items(); track item.route) {
        <ui-nav-item [item]="item" layout="bottom" />
      }
    </nav>
  `,
  styles: `
    :host { display: block; }
    @media (min-width: 768px) { :host { display: none; } }

    .bottom-nav {
      position: fixed;
      bottom: 0;
      left: 0;
      width: 100%;
      z-index: 50;
      display: grid;
      grid-auto-flow: column;
      grid-auto-columns: 1fr;
      align-items: stretch;
      height: 64px;
      background: var(--cp-surface-container-lowest);
      border-top: 1px solid var(--cp-outline-variant);
    }
  `,
})
export class MobileBottomNav {
  private readonly navigation = inject(NavigationService);

  protected readonly items = computed(() => this.navigation.primaryItems().filter((item) => item.mobile));
}
