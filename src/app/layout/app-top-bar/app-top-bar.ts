import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SessionService } from '../../core/services/session.service';
import { MessagesService } from '../../core/services/messages.service';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { ThemeToggle } from '../theme-toggle/theme-toggle';
import { NotificationsMenu } from '../notifications-menu/notifications-menu';
import { AccountMenu } from '../account-menu/account-menu';
import { LanguageSwitcher } from '../language-switcher/language-switcher';

/**
 * Responsive top app bar. On mobile it also carries the brand; on desktop the
 * side navigation owns the brand and this bar hosts the action cluster:
 * notifications, messages, theme and the account menu.
 */
@Component({
  selector: 'app-top-bar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, UiIcon, ThemeToggle, NotificationsMenu, AccountMenu, LanguageSwitcher],
  templateUrl: './app-top-bar.html',
  styleUrl: './app-top-bar.scss',
})
export class AppTopBar {
  private readonly session = inject(SessionService);
  private readonly messagesService = inject(MessagesService);

  protected readonly isAuthenticated = this.session.isAuthenticated;
  protected readonly canMessage = computed(() => this.session.can('message'));
  protected readonly unreadMessages = this.messagesService.totalUnread;
}
