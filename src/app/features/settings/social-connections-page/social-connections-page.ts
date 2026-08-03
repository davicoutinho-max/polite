import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PageHeader } from '../../../shared/ui/page-header/page-header';
import { UiButton } from '../../../shared/ui/ui-button/ui-button';
import { UiTag } from '../../../shared/ui/ui-tag/ui-tag';
import { UiIcon } from '../../../shared/ui/ui-icon/ui-icon';
import { UiSkeleton } from '../../../shared/ui/ui-skeleton/ui-skeleton';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';
import { TranslateService } from '../../../core/services/translate.service';
import { AlertsService } from '../../../core/services/alerts.service';
import { SocialConnection, SocialConnectionService, SocialPlatform } from '../../../core/services/social-connection.service';

/** Real Meta/X OAuth connect/disconnect management — see SocialConnectionService's javadoc-style
 * comment for why connecting is a full browser redirect rather than an in-page API call. This page
 * is also where the browser lands back after that redirect (query params `connected`/`error`). */
@Component({
  selector: 'app-social-connections-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeader, UiButton, UiTag, UiIcon, UiSkeleton, TranslatePipe],
  templateUrl: './social-connections-page.html',
  styleUrl: './social-connections-page.scss',
})
export class SocialConnectionsPage {
  private readonly socialConnections = inject(SocialConnectionService);
  private readonly route = inject(ActivatedRoute);
  private readonly alerts = inject(AlertsService);
  protected readonly translate = inject(TranslateService);

  protected readonly connections = signal<SocialConnection[]>([]);
  protected readonly loading = signal(true);
  protected readonly connecting = signal<'meta' | 'x' | null>(null);
  protected readonly metaPlatforms: SocialPlatform[] = ['facebook', 'instagram'];

  constructor() {
    this.reload();

    const params = this.route.snapshot.queryParamMap;
    const connected = params.get('connected');
    const error = params.get('error');
    if (connected) {
      this.alerts.push({
        category: 'project',
        icon: 'check_circle',
        title: this.translate.t('title.social-connected', 'Connected!'),
        message: connected
          .split(',')
          .map((code) => this.platformLabel(code as SocialPlatform))
          .join(', '),
        timeLabel: this.translate.t('label.just-now', 'Just now'),
      });
    } else if (error) {
      this.alerts.push({
        category: 'project',
        icon: 'error',
        title: this.translate.t('title.social-connect-failed', 'Connection failed'),
        message: params.get('reason') ?? this.translate.t('hint.social-connect-failed', 'Could not connect — please try again.'),
        timeLabel: this.translate.t('label.just-now', 'Just now'),
      });
    }
  }

  private reload(): void {
    this.loading.set(true);
    this.socialConnections.listConnections().subscribe({
      next: (list) => {
        this.connections.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected connectionFor(platform: SocialPlatform): SocialConnection | undefined {
    return this.connections().find((c) => c.platform === platform);
  }

  protected platformLabel(platform: SocialPlatform): string {
    return { facebook: 'Facebook', instagram: 'Instagram', x: 'X' }[platform];
  }

  protected connectMeta(): void {
    this.connecting.set('meta');
    this.socialConnections.startMetaConnect().subscribe({
      next: (url) => (window.location.href = url),
      error: () => {
        this.connecting.set(null);
        this.alerts.push({
          category: 'project',
          icon: 'error',
          title: this.translate.t('title.social-connect-failed', 'Connection failed'),
          message: this.translate.t('hint.social-connect-failed', 'Could not connect — please try again.'),
          timeLabel: this.translate.t('label.just-now', 'Just now'),
        });
      },
    });
  }

  protected connectX(): void {
    this.connecting.set('x');
    this.socialConnections.startXConnect().subscribe({
      next: (url) => (window.location.href = url),
      error: () => {
        this.connecting.set(null);
        this.alerts.push({
          category: 'project',
          icon: 'error',
          title: this.translate.t('title.social-connect-failed', 'Connection failed'),
          message: this.translate.t('hint.social-connect-failed', 'Could not connect — please try again.'),
          timeLabel: this.translate.t('label.just-now', 'Just now'),
        });
      },
    });
  }

  protected disconnect(platform: SocialPlatform): void {
    this.socialConnections.disconnect(platform).subscribe({
      next: () => this.connections.update((list) => list.filter((c) => c.platform !== platform)),
      error: () =>
        this.alerts.push({
          category: 'project',
          icon: 'error',
          title: this.translate.t('title.social-connect-failed', 'Connection failed'),
          message: this.translate.t('error.disconnect-failed', 'Could not disconnect — please try again.'),
          timeLabel: this.translate.t('label.just-now', 'Just now'),
        }),
    });
  }
}
