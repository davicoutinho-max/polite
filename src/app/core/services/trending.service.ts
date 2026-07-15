import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { TrendingTopic } from '../models';

interface TrendingTopicResponseDto {
  readonly hashtag: string;
  readonly postCountLast24h: number;
  readonly rank: number;
}

function abbreviateCount(count: number): string {
  if (count >= 1_000_000) {
    return `${(count / 1_000_000).toFixed(1).replace(/\.0$/, '')}M posts`;
  }
  if (count >= 1_000) {
    return `${(count / 1_000).toFixed(1).replace(/\.0$/, '')}K posts`;
  }
  return `${count} posts`;
}

@Injectable({ providedIn: 'root' })
export class TrendingService {
  private readonly http = inject(HttpClient);
  private readonly apiBase = `${environment.apiBaseUrl}/api/feed`;

  private readonly _topics = signal<TrendingTopic[]>([]);
  readonly topics = this._topics.asReadonly();

  constructor() {
    this.reload().subscribe();
  }

  reload(limit = 10): Observable<TrendingTopic[]> {
    return this.http.get<TrendingTopicResponseDto[]>(`${this.apiBase}/trending`, { params: { limit } }).pipe(
      map((list) =>
        list.map(
          (t): TrendingTopic => ({
            id: t.hashtag,
            rank: t.rank,
            category: 'Trending',
            title: `#${t.hashtag}`,
            postCount: abbreviateCount(t.postCountLast24h),
          }),
        ),
      ),
      tap((topics) => this._topics.set(topics)),
    );
  }
}
