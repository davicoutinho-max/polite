import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SessionService } from '../../core/services/session.service';
import { ACCOUNT_TYPE_OPTIONS, AccountType } from '../../core/models';
import { UiAvatar } from '../../shared/ui/ui-avatar/ui-avatar';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/**
 * Account button with a dropdown. Exposes the current identity plus an
 * account-type switcher so each flow (visitor / citizen / politician / party /
 * admin) can be exercised live across the whole UI.
 */
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
  protected readonly type = this.session.type;
  protected readonly isAuthenticated = this.session.isAuthenticated;
  protected readonly canFollow = computed(() => this.session.can('follow'));
  protected readonly typeOptions = ACCOUNT_TYPE_OPTIONS;
  protected readonly open = signal(false);

  protected toggle(): void {
    this.open.update((v) => !v);
  }

  protected close(): void {
    this.open.set(false);
  }

  protected selectType(type: AccountType): void {
    this.close();
    this.session.loginAsDemo(type).subscribe({ error: () => undefined });
  }

  protected logout(): void {
    this.session.logout();
    this.close();
  }
}
