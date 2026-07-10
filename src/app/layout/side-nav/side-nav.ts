import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavigationService } from '../../core/services/navigation.service';
import { SessionService } from '../../core/services/session.service';
import { ThemeService } from '../../core/services/theme.service';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { UiNavItem } from '../../shared/ui/ui-nav-item/ui-nav-item';
import { UiAvatar } from '../../shared/ui/ui-avatar/ui-avatar';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Desktop persistent side navigation. */
@Component({
  selector: 'app-side-nav',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, UiButton, UiNavItem, UiAvatar, UiIcon, TranslatePipe],
  templateUrl: './side-nav.html',
  styleUrl: './side-nav.scss',
})
export class SideNav {
  private readonly navigation = inject(NavigationService);
  private readonly session = inject(SessionService);
  protected readonly theme = inject(ThemeService);

  protected readonly primaryItems = this.navigation.primaryItems;
  protected readonly secondaryItems = this.navigation.secondaryItems;
  protected readonly currentUser = this.session.currentUser;
  protected readonly isAuthenticated = this.session.isAuthenticated;
  /** Politicians/parties publish; everyone else engages by commenting. */
  protected readonly canPublish = computed(() => this.session.can('publish-content'));
}
