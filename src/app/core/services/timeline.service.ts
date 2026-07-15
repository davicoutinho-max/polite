import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, forkJoin, map, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { TimelineEvent, TimelineEventType } from '../models';
import { relativeTime } from '../utils/relative-time';
import { DirectoryService } from './directory.service';
import { SessionService } from './session.service';

interface EventVisual {
  readonly icon: string;
  readonly severity: 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'info' | 'neutral';
}

/** Icon + colour language for each timeline event type. */
export const TIMELINE_VISUALS: Record<TimelineEventType, EventVisual> = {
  vote: { icon: 'how_to_vote', severity: 'success' },
  project: { icon: 'description', severity: 'secondary' },
  pec: { icon: 'history_edu', severity: 'warning' },
  cpi: { icon: 'search', severity: 'info' },
  status_change: { icon: 'sync', severity: 'secondary' },
  committee: { icon: 'gavel', severity: 'primary' },
  video: { icon: 'smart_display', severity: 'info' },
  post: { icon: 'forum', severity: 'secondary' },
  party_change: { icon: 'sync_alt', severity: 'warning' },
  campaign: { icon: 'campaign', severity: 'primary' },
};

interface TimelineEventResponseDto {
  readonly id: string;
  readonly type: string;
  readonly title: string;
  readonly detail: string | null;
  readonly occurredAt: string;
  readonly group: string;
  readonly actorAccountId: string;
  readonly actorName: string | null;
}

/**
 * Activity timeline — merges activity-feed-service's per-subject timeline for every account the
 * current user follows (plus their own), since the real endpoint is scoped to one subject at a
 * time. Purely a client-side fan-out + merge, no server-side aggregation exists.
 */
@Injectable({ providedIn: 'root' })
export class TimelineService {
  private readonly http = inject(HttpClient);
  private readonly directory = inject(DirectoryService);
  private readonly session = inject(SessionService);
  private readonly apiBase = `${environment.apiBaseUrl}/api/activity-feed`;

  private readonly _events = signal<TimelineEvent[]>([]);
  readonly events = this._events.asReadonly();

  /** Events grouped by their bucket, preserving order. */
  readonly grouped = computed(() => {
    const groups = new Map<string, TimelineEvent[]>();
    for (const event of this._events()) {
      const bucket = groups.get(event.group) ?? [];
      bucket.push(event);
      groups.set(event.group, bucket);
    }
    return [...groups.entries()].map(([group, events]) => ({ group, events }));
  });

  constructor() {
    this.reload().subscribe();
  }

  reload(limit = 20): Observable<TimelineEvent[]> {
    const followed = [...this.directory.followingPoliticians(), ...this.directory.followingParties()];
    const selfId = this.session.isAuthenticated() ? this.session.account().id : null;
    const subjectIds = [...new Set(selfId ? [selfId, ...followed] : followed)];

    if (!subjectIds.length) {
      this._events.set([]);
      return of([]);
    }

    return forkJoin(
      subjectIds.map((id) =>
        this.http.get<TimelineEventResponseDto[]>(`${this.apiBase}/timeline`, { params: { subjectAccountId: id, limit } }),
      ),
    ).pipe(
      map((lists) =>
        lists
          .flat()
          .sort((a, b) => new Date(b.occurredAt).getTime() - new Date(a.occurredAt).getTime())
          .slice(0, limit)
          .map(
            (dto): TimelineEvent => ({
              id: dto.id,
              type: dto.type as TimelineEventType,
              title: dto.title,
              detail: dto.detail ?? undefined,
              timeLabel: relativeTime(dto.occurredAt),
              group: dto.group,
              actor: dto.actorName ?? 'Unknown',
            }),
          ),
      ),
      tap((events) => this._events.set(events)),
    );
  }
}
