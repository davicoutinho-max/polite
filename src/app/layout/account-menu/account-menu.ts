import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SessionService } from '../../core/services/session.service';
import { UiAvatar } from '../../shared/ui/ui-avatar/ui-avatar';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Account button with a dropdown: current identity, profile/privacy links, sign out. */
@Component({
  selector: 'app-account-menu',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, UiAvatar, UiIcon, TranslatePipe],
  templateUrl: './account-menu.html',
  styleUrl: './account-menu.scss',
})
export class AccountMenu {
  private readonly session = inject(SessionService);

  protected readonly account = this.session.account;
  protected readonly isAuthenticated = this.session.isAuthenticated;
  protected readonly canFollow = computed(() => this.session.can('follow'));
  protected readonly open = signal(false);

  protected toggle(): void {
    this.open.update((v) => !v);
  }

  protected close(): void {
    this.open.set(false);
  }

  protected logout(): void {
    this.session.logout();
    this.close();
  }
}
