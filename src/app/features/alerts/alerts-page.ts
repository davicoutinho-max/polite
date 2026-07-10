import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AlertsService } from '../../core/services/alerts.service';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiEmpty } from '../../shared/ui/ui-empty/ui-empty';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Notification / alerts center. */
@Component({
  selector: 'app-alerts-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeader, UiButton, UiIcon, UiEmpty, TranslatePipe],
  templateUrl: './alerts-page.html',
  styleUrl: './alerts-page.scss',
})
export class AlertsPage {
  private readonly alertsService = inject(AlertsService);

  protected readonly alerts = this.alertsService.alerts;
  protected readonly unreadCount = this.alertsService.unreadCount;

  protected onRead(id: string): void {
    this.alertsService.markRead(id);
  }

  protected onReadAll(): void {
    this.alertsService.markAllRead();
  }
}
