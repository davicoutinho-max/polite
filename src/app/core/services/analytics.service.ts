import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, forkJoin, map, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AreaPoint } from '../../shared/ui/ui-area-chart/ui-area-chart';
import { BarDatum } from '../../shared/ui/ui-bar-chart/ui-bar-chart';
import { SessionService } from './session.service';

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

const CONTENT_TYPE_LABELS: Record<string, string> = {
  text: 'Text posts',
  video: 'Videos',
  agenda: 'Agenda',
  live: 'Live sessions',
};

const ACCOUNT_TYPE_LABELS: Record<string, string> = {
  citizen: 'Citizens',
  politician: 'Politicians',
  party: 'Parties',
  admin: 'Platform admins',
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
    this.reload().subscribe();
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
        this._kpis.set([
          { icon: 'visibility', label: 'Reach', value: abbreviate(kpis.reach), caption: 'Distinct accounts engaged' },
          { icon: 'group', label: 'Net follows', value: `${kpis.netFollows >= 0 ? '+' : ''}${kpis.netFollows}`, caption: 'Last 30 days' },
          { icon: 'favorite', label: 'Engagement', value: `${kpis.engagementRatePercent.toFixed(1)}%`, caption: 'Likes + comments / reach' },
          { icon: 'forum', label: 'Interactions', value: abbreviate(kpis.totalLikes + kpis.totalComments), caption: `${kpis.totalPosts} posts` },
        ]);
        this._engagement.set(
          engagement.map(
            (e): AreaPoint => ({
              label: new Date(`${e.day}T00:00:00`).toLocaleDateString('en-US', { weekday: 'short' }),
              value: e.likes + e.comments,
            }),
          ),
        );
        this._byContentType.set(
          byContentType.map(
            (t): BarDatum => ({ label: (t.key && CONTENT_TYPE_LABELS[t.key]) ?? t.key ?? 'Unknown', value: t.count, display: abbreviate(t.count) }),
          ),
        );
        this._byAccountType.set(
          byAccountType.map(
            (t): BarDatum => ({ label: (t.key && ACCOUNT_TYPE_LABELS[t.key]) ?? t.key ?? 'Unknown', value: t.count, display: abbreviate(t.count) }),
          ),
        );
      }),
      map(() => undefined),
    );
  }
}
