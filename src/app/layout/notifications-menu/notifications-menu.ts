import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AlertsService } from '../../core/services/alerts.service';
import { AlertCategory, TagSeverity } from '../../core/models';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';

const CATEGORY_TONE: Record<AlertCategory, TagSeverity> = {
  project: 'info',
  party: 'secondary',
  pec: 'primary',
  cpi: 'warning',
  vote: 'success',
  campaign: 'danger',
};

/** Social-style notification bell with a dropdown feed of recent alerts. */
@Component({
  selector: 'app-notifications-menu',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, UiIcon],
  templateUrl: './notifications-menu.html',
  styleUrl: './notifications-menu.scss',
})
export class NotificationsMenu {
  private readonly alertsService = inject(AlertsService);

  protected readonly alerts = this.alertsService.alerts;
  protected readonly unread = this.alertsService.unreadCount;
  protected readonly open = signal(false);

  protected toggle(): void {
    this.open.update((v) => !v);
  }

  protected close(): void {
    this.open.set(false);
  }

  protected onSelect(id: string): void {
    this.alertsService.markRead(id);
    this.close();
  }

  protected markAllRead(): void {
    this.alertsService.markAllRead();
  }

  protected tone(category: AlertCategory): TagSeverity {
    return CATEGORY_TONE[category] ?? 'neutral';
  }
}
