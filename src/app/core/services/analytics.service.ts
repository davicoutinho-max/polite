import { HttpClient } from '@angular/common/http';
import { effect, inject, Injectable, signal } from '@angular/core';
import { Observable, forkJoin, map, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AreaPoint } from '../../shared/ui/ui-area-chart/ui-area-chart';
import { BarDatum } from '../../shared/ui/ui-bar-chart/ui-bar-chart';
import { SessionService } from './session.service';
import { TranslateService } from './translate.service';

export interface AnalyticsKpi {
  readonly icon: string;
  readonly label: string;
  readonly value: string;
  readonly caption: string;
}

interface KpiSummaryResponseDto {
  readonly totalPosts: number;
  readonly totalLikes: number;
  readonly totalComments: number;
  readonly netFollows: number;
  readonly reach: number;
  readonly engagementRatePercent: number;
}

interface DailyEngagementResponseDto {
  readonly day: string;
  readonly likes: number;
  readonly comments: number;
}

interface TypeCountResponseDto {
  readonly key: string | null;
  readonly count: number;
}

const CONTENT_TYPE_LABEL_KEYS: Record<string, [string, string]> = {
  text: ['label.content-type-text', 'Text posts'],
  video: ['label.content-type-video', 'Videos'],
  agenda: ['label.content-type-agenda', 'Agenda'],
  live: ['label.content-type-live', 'Live sessions'],
};

const ACCOUNT_TYPE_LABEL_KEYS: Record<string, [string, string]> = {
  citizen: ['label.account-type-citizen', 'Citizens'],
  politician: ['label.account-type-politician', 'Politicians'],
  party: ['label.account-type-party', 'Parties'],
  admin: ['label.account-type-admin', 'Platform admins'],
};

function abbreviate(n: number): string {
  if (n >= 1_000_000) {
    return `${(n / 1_000_000).toFixed(1).replace(/\.0$/, '')}M`;
  }
  if (n >= 1_000) {
    return `${(n / 1_000).toFixed(1).replace(/\.0$/, '')}k`;
  }
  return `${n}`;
}

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly translate = inject(TranslateService);
  private readonly apiBase = `${environment.apiBaseUrl}/api/analytics`;

  private readonly _kpis = signal<AnalyticsKpi[]>([]);
  readonly kpis = this._kpis.asReadonly();

  private readonly _engagement = signal<AreaPoint[]>([]);
  readonly engagement = this._engagement.asReadonly();

  private readonly _byContentType = signal<BarDatum[]>([]);
  readonly byContentType = this._byContentType.asReadonly();

  private readonly _byAccountType = signal<BarDatum[]>([]);
  readonly byAccountType = this._byAccountType.asReadonly();

  constructor() {
    // See DirectoryService's reloadFollowing for why this waits on session.ready() instead of
    // firing immediately — otherwise a hard refresh can permanently strand this dashboard empty.
    effect(() => {
      if (this.session.ready()) {
        this.reload().subscribe();
      }
    });
  }

  reload(): Observable<void> {
    if (!this.session.isAuthenticated()) {
      return of(undefined);
    }
    const authorId = this.session.account().id;
    return forkJoin({
      kpis: this.http.get<KpiSummaryResponseDto>(`${this.apiBase}/${authorId}/kpis`),
      engagement: this.http.get<DailyEngagementResponseDto[]>(`${this.apiBase}/${authorId}/engagement`, { params: { days: 7 } }),
      byContentType: this.http.get<TypeCountResponseDto[]>(`${this.apiBase}/${authorId}/by-content-type`),
      byAccountType: this.http.get<TypeCountResponseDto[]>(`${this.apiBase}/${authorId}/by-account-type`),
    }).pipe(
      tap(({ kpis, engagement, byContentType, byAccountType }) => {
        const t = (key: string, fallback: string) => this.translate.t(key, fallback);
        this._kpis.set([
          { icon: 'visibility', label: t('label.reach', 'Reach'), value: abbreviate(kpis.reach), caption: t('label.reach-caption', 'Distinct accounts engaged') },
          {
            icon: 'group',
            label: t('label.net-follows', 'Net follows'),
            value: `${kpis.netFollows >= 0 ? '+' : ''}${kpis.netFollows}`,
            caption: t('label.last-30-days', 'Last 30 days'),
          },
          {
            icon: 'favorite',
            label: t('label.engagement', 'Engagement'),
            value: `${kpis.engagementRatePercent.toFixed(1)}%`,
            caption: t('label.engagement-caption', 'Likes + comments / reach'),
          },
          {
            icon: 'forum',
            label: t('label.interactions', 'Interactions'),
            value: abbreviate(kpis.totalLikes + kpis.totalComments),
            caption: `${kpis.totalPosts} ${t('label.posts', 'posts')}`,
          },
        ]);
        this._engagement.set(
          engagement.map(
            (e): AreaPoint => ({
              label: new Date(`${e.day}T00:00:00`).toLocaleDateString('en-US', { weekday: 'short' }),
              value: e.likes + e.comments,
            }),
          ),
        );
        const unknown = this.translate.t('label.unknown', 'Unknown');
        this._byContentType.set(
          byContentType.map((c): BarDatum => {
            const meta = c.key ? CONTENT_TYPE_LABEL_KEYS[c.key] : undefined;
            return { label: meta ? this.translate.t(meta[0], meta[1]) : (c.key ?? unknown), value: c.count, display: abbreviate(c.count) };
          }),
        );
        this._byAccountType.set(
          byAccountType.map((a): BarDatum => {
            const meta = a.key ? ACCOUNT_TYPE_LABEL_KEYS[a.key] : undefined;
            return { label: meta ? this.translate.t(meta[0], meta[1]) : (a.key ?? unknown), value: a.count, display: abbreviate(a.count) };
          }),
        );
      }),
      map(() => undefined),
    );
  }
}
