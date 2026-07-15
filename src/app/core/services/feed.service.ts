import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { EMPTY, Observable, catchError, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { FeedSort, Post, PostComment, PostDraft, PostKind, PostVisibility, StatusTag, TagSeverity, UserSummary } from '../models';
import { relativeTime } from '../utils/relative-time';
import { DirectoryService } from './directory.service';
import { SessionService } from './session.service';

interface PostTagResponse {
  readonly label: string;
  readonly severity: string | null;
  readonly icon: string | null;
}

interface PostResponseDto {
  readonly id: string;
  readonly authorAccountId: string;
  readonly kind: string;
  readonly content: string | null;
  readonly imageUrl: string | null;
  readonly visibility: string;
  readonly context: string | null;
  readonly liveSessionId: string | null;
  readonly createdAt: string;
  readonly tags: PostTagResponse[];
  readonly agendaTitle: string | null;
  readonly agendaEventDate: string | null;
  readonly agendaLocation: string | null;
}

interface PostMetricsResponseDto {
  readonly likesCount: number;
  readonly commentsCount: number;
}

interface CommentResponseDto {
  readonly id: string;
  readonly authorAccountId: string;
  readonly body: string;
  readonly createdAt: string;
}

interface LiveSessionResponseDto {
  readonly id: string;
  readonly videoId: string | null;
  readonly channelId: string | null;
  readonly status: string;
  readonly scheduledFor: string | null;
  readonly peakViewers: number;
}

interface AccountResponseDto {
  readonly id: string;
  readonly name: string;
  readonly handle: string;
  readonly avatarUrl: string;
  readonly verified: boolean;
}

/** Sidebar "live now" widget data — sourced from live-streaming-service. */
export interface LiveNowInfo {
  readonly videoId: string | null;
  readonly channelId: string | null;
  readonly watching: number;
}

/**
 * Reactive feed store. Posts/comments/likes/tags live in feed-content-service;
 * "live" posts additionally reference a live-streaming-service session. Author
 * display data is resolved client-side against DirectoryService's already-loaded
 * politician/party cache, falling back to identity-service for other account types.
 */
@Injectable({ providedIn: 'root' })
export class FeedService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly directory = inject(DirectoryService);
  private readonly feedApiBase = `${environment.apiBaseUrl}/api/feed`;
  private readonly liveApiBase = `${environment.apiBaseUrl}/api/live`;
  private readonly identityApiBase = `${environment.apiBaseUrl}/api/identity`;

  private readonly authorCache = new Map<string, UserSummary>();

  private readonly _posts = signal<Post[]>([]);
  private readonly _sort = signal<FeedSort>('top');
  private readonly _liveNow = signal<LiveNowInfo | null>(null);

  readonly sort = this._sort.asReadonly();
  readonly liveNow = this._liveNow.asReadonly();

  /** Posts ordered according to the active sort. */
  readonly posts = computed<Post[]>(() => {
    const posts = [...this._posts()];
    switch (this._sort()) {
      case 'top':
        return posts.sort((a, b) => b.metrics.likes - a.metrics.likes);
      case 'latest':
        return posts;
      case 'following':
        return posts.filter((p) => p.author.verified);
    }
  });

  constructor() {
    this.reloadFeed().subscribe();
    this.reloadLiveNow().subscribe();
  }

  setSort(sort: FeedSort): void {
    this._sort.set(sort);
  }

  /** Posts authored by a given account id, e.g. for a profile's Activity tab. */
  postsByAuthor(authorId: string) {
    return computed(() => this._posts().filter((p) => p.author.id === authorId));
  }

  reloadFeed(page = 0, pageSize = 50): Observable<Post[]> {
    return this.http.get<PostResponseDto[]>(`${this.feedApiBase}/posts`, { params: { page, pageSize } }).pipe(
      switchMap((list) => (list.length ? forkJoin(list.map((dto) => this.toPost(dto))) : of([]))),
      tap((posts) => this._posts.set(posts)),
    );
  }

  reloadLiveNow(): Observable<LiveNowInfo | null> {
    return this.http.get<LiveSessionResponseDto[]>(`${this.liveApiBase}/live-sessions/live`).pipe(
      map((sessions) => (sessions.length ? sessions[0] : null)),
      map((session) => (session ? { videoId: session.videoId, channelId: session.channelId, watching: session.peakViewers } : null)),
      tap((info) => this._liveNow.set(info)),
      catchError(() => {
        this._liveNow.set(null);
        return of(null);
      }),
    );
  }

  toggleLike(postId: string): void {
    const post = this._posts().find((p) => p.id === postId);
    if (!post) {
      return;
    }
    const accountId = this.session.account().id;
    const request$ = post.metrics.liked
      ? this.http.delete<void>(`${this.feedApiBase}/posts/${postId}/likes`, { params: { accountId } })
      : this.http.post<void>(`${this.feedApiBase}/posts/${postId}/likes`, { accountId });

    request$.subscribe({
      next: () =>
        this._posts.update((posts) =>
          posts.map((p) =>
            p.id === postId
              ? { ...p, metrics: { ...p.metrics, liked: !p.metrics.liked, likes: p.metrics.likes + (p.metrics.liked ? -1 : 1) } }
              : p,
          ),
        ),
    });
  }

  publish(draft: PostDraft): Observable<Post | void> {
    const visibility = draft.visibility;

    if (draft.kind === 'agenda' && draft.agenda) {
      return this.http
        .post<PostResponseDto>(`${this.feedApiBase}/posts/agenda`, {
          title: draft.agenda.title,
          eventDate: draft.agenda.date,
          location: draft.agenda.location,
          visibility,
          context: draft.agenda.location,
        })
        .pipe(switchMap((dto) => this.tagAndReload(dto.id, 'agenda', visibility)));
    }

    if (draft.kind === 'live' && draft.live) {
      return this.scheduleLiveSession(draft.live).pipe(
        switchMap((session) =>
          this.http
            .post<PostResponseDto>(`${this.feedApiBase}/posts/live`, {
              liveSessionId: session.id,
              visibility,
              context: draft.live!.isLiveNow ? 'Live now' : 'Scheduled live',
            })
            .pipe(
              switchMap((dto) =>
                this.http.post(`${this.liveApiBase}/live-sessions/${session.id}/post`, { postId: dto.id }).pipe(map(() => dto)),
              ),
            ),
        ),
        switchMap((dto) => this.tagAndReload(dto.id, 'live', visibility)),
        tap(() => this.reloadLiveNow().subscribe()),
      );
    }

    const text = draft.text.trim();
    if (!text) {
      return EMPTY;
    }
    return this.http
      .post<PostResponseDto>(`${this.feedApiBase}/posts/text`, {
        content: text,
        visibility,
        context: visibility === 'private' ? 'Private' : 'Your feed',
      })
      .pipe(switchMap((dto) => this.tagAndReload(dto.id, 'text', visibility)));
  }

  addComment(postId: string, text: string): void {
    const body = text.trim();
    if (!body) {
      return;
    }
    const authorAccountId = this.session.account().id;
    this.http
      .post<CommentResponseDto>(`${this.feedApiBase}/posts/${postId}/comments`, { authorAccountId, body })
      .pipe(
        switchMap((c) =>
          this.resolveAuthor(c.authorAccountId).pipe(
            map((author): PostComment => ({ id: c.id, author, text: c.body, timeLabel: relativeTime(c.createdAt) })),
          ),
        ),
      )
      .subscribe({
        next: (comment) =>
          this._posts.update((posts) =>
            posts.map((p) =>
              p.id === postId
                ? { ...p, metrics: { ...p.metrics, comments: p.metrics.comments + 1 }, comments: [...p.comments, comment] }
                : p,
            ),
          ),
      });
  }

  private scheduleLiveSession(live: {
    videoId: string;
    channelId: string;
    isLiveNow: boolean;
    scheduledFor: string;
  }): Observable<LiveSessionResponseDto> {
    return this.http
      .post<LiveSessionResponseDto>(`${this.liveApiBase}/live-sessions`, {
        videoId: live.videoId || null,
        channelId: live.channelId || null,
        scheduledFor: live.isLiveNow ? null : live.scheduledFor ? new Date(live.scheduledFor).toISOString() : null,
      })
      .pipe(
        switchMap((session) =>
          live.isLiveNow
            ? this.http.post<LiveSessionResponseDto>(`${this.liveApiBase}/live-sessions/${session.id}/start`, {})
            : of(session),
        ),
      );
  }

  private tagAndReload(postId: string, kind: PostKind, visibility: PostVisibility): Observable<Post> {
    const tags = this.tagsFor(kind, visibility);
    return forkJoin(tags.map((t) => this.http.post(`${this.feedApiBase}/posts/${postId}/tags`, t))).pipe(
      switchMap(() => this.http.get<PostResponseDto>(`${this.feedApiBase}/posts/${postId}`)),
      switchMap((dto) => this.toPost(dto)),
      tap((post) => this._posts.update((posts) => [post, ...posts])),
    );
  }

  private tagsFor(kind: PostKind, visibility: PostVisibility): { label: string; severity: TagSeverity; icon?: string }[] {
    const tags: { label: string; severity: TagSeverity; icon?: string }[] = [];
    switch (kind) {
      case 'agenda':
        tags.push({ label: '#Agenda', severity: 'info', icon: 'event' });
        break;
      case 'live':
        tags.push({ label: '#Live', severity: 'danger', icon: 'sensors' });
        break;
      default:
        tags.push({ label: '#Discussion', severity: 'secondary' });
    }
    if (visibility === 'private') {
      tags.push({ label: 'Private', severity: 'neutral', icon: 'lock' });
    }
    return tags;
  }

  private toPost(dto: PostResponseDto): Observable<Post> {
    const isLive = dto.kind === 'live' && !!dto.liveSessionId;
    return forkJoin({
      author: this.resolveAuthor(dto.authorAccountId),
      comments: this.http.get<CommentResponseDto[]>(`${this.feedApiBase}/posts/${dto.id}/comments`).pipe(
        switchMap((comments) =>
          comments.length
            ? forkJoin(
                comments.map((c) =>
                  this.resolveAuthor(c.authorAccountId).pipe(
                    map((author): PostComment => ({ id: c.id, author, text: c.body, timeLabel: relativeTime(c.createdAt) })),
                  ),
                ),
              )
            : of([] as PostComment[]),
        ),
      ),
      metrics: this.http.get<PostMetricsResponseDto>(`${this.feedApiBase}/posts/${dto.id}/metrics`),
      liked: this.session.isAuthenticated()
        ? this.http
            .get<boolean>(`${this.feedApiBase}/posts/${dto.id}/likes/${this.session.account().id}`)
            .pipe(catchError(() => of(false)))
        : of(false),
      live: isLive
        ? this.http.get<LiveSessionResponseDto>(`${this.liveApiBase}/live-sessions/${dto.liveSessionId}`).pipe(catchError(() => of(null)))
        : of(null),
    }).pipe(
      map(({ author, comments, metrics, liked, live }): Post => {
        const tags: StatusTag[] = dto.tags.map((t) => ({
          label: t.label,
          severity: (t.severity ?? 'neutral') as TagSeverity,
          icon: t.icon ?? undefined,
        }));
        return {
          id: dto.id,
          author,
          createdAt: relativeTime(dto.createdAt),
          context: dto.context ?? '',
          content: dto.content ?? '',
          tags,
          kind: dto.kind as PostKind,
          imageUrl: dto.imageUrl ?? undefined,
          agenda:
            dto.kind === 'agenda' && dto.agendaTitle && dto.agendaEventDate && dto.agendaLocation
              ? { title: dto.agendaTitle, date: dto.agendaEventDate, location: dto.agendaLocation }
              : undefined,
          live: live
            ? {
                videoId: live.videoId ?? undefined,
                channelId: live.channelId ?? undefined,
                isLiveNow: live.status === 'live',
                scheduledFor: live.scheduledFor ?? undefined,
              }
            : undefined,
          visibility: dto.visibility as PostVisibility,
          metrics: { likes: metrics.likesCount, comments: metrics.commentsCount, liked },
          comments,
        };
      }),
    );
  }

  private resolveAuthor(accountId: string): Observable<UserSummary> {
    const cached = this.authorCache.get(accountId);
    if (cached) {
      return of(cached);
    }
    const politician = this.directory.politicians().find((p) => p.id === accountId);
    if (politician) {
      const author: UserSummary = {
        id: politician.id,
        name: politician.name,
        handle: politician.handle,
        avatarUrl: politician.avatarUrl,
        verified: politician.verified,
        role: politician.office,
      };
      this.authorCache.set(accountId, author);
      return of(author);
    }
    const party = this.directory.parties().find((p) => p.id === accountId);
    if (party) {
      const author: UserSummary = {
        id: party.id,
        name: party.name,
        avatarUrl: party.logoUrl,
        verified: true,
        role: 'Official party account',
      };
      this.authorCache.set(accountId, author);
      return of(author);
    }
    return this.http.get<AccountResponseDto>(`${this.identityApiBase}/accounts/${accountId}`).pipe(
      map(
        (r): UserSummary => ({
          id: r.id,
          name: r.name,
          handle: r.handle,
          avatarUrl: r.avatarUrl,
          verified: r.verified,
        }),
      ),
      tap((author) => this.authorCache.set(accountId, author)),
      catchError(() => of({ id: accountId, name: 'Unknown', avatarUrl: '', verified: false })),
    );
  }
}
