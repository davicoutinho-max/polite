import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { EMPTY, Observable, catchError, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { FeedSort, Post, PostComment, PostDraft, PostKind, PostPollOption, PostVisibility, StatusTag, TagSeverity, UserSummary } from '../models';
import { relativeTime } from '../utils/relative-time';
import { DirectoryService } from './directory.service';
import { SessionService } from './session.service';

interface PostTagResponse {
  readonly label: string;
  readonly severity: string | null;
  readonly icon: string | null;
}

interface PollOptionResponseDto {
  readonly id: string;
  readonly label: string;
  readonly votes: number;
}

interface PostResponseDto {
  readonly id: string;
  readonly authorAccountId: string;
  readonly kind: string;
  readonly content: string | null;
  readonly imageUrl: string | null;
  readonly fileUrl: string | null;
  readonly fileName: string | null;
  readonly visibility: string;
  readonly context: string | null;
  readonly liveSessionId: string | null;
  readonly createdAt: string;
  readonly tags: PostTagResponse[];
  readonly agendaTitle: string | null;
  readonly agendaEventDate: string | null;
  readonly agendaLocation: string | null;
  readonly pollOptions: PollOptionResponseDto[];
}

interface MediaUploadResponseDto {
  readonly url: string;
  readonly fileName: string;
}

/** Extras resolved before the actual create-post call — any image/file draft the composer
 * attached is uploaded first so the post can be created with its final URL in one shot. */
interface ResolvedAttachments {
  readonly imageUrl?: string;
  readonly fileUrl?: string;
  readonly fileName?: string;
  readonly pollOptions?: string[];
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
      switchMap((list) =>
        list.length
          ? forkJoin(
              // One bad post (e.g. a stale row missing its metrics/comments join) must never
              // blank the whole feed — forkJoin fails the entire batch on a single member
              // erroring, so each post is isolated here and dropped on its own if it fails.
              list.map((dto) => this.toPost(dto).pipe(catchError(() => of(null)))),
            )
          : of([]),
      ),
      map((posts) => posts.filter((p): p is Post => p !== null)),
      tap((posts) => this._posts.set(posts)),
    );
  }

  /** Only the post's own author can call this in practice — post-card only renders the delete
   * option for their own posts — but the backend re-checks ownership regardless (via the
   * gateway-injected X-Account-Id header, same as post creation). */
  deletePost(postId: string): Observable<void> {
    return this.http.delete<void>(`${this.feedApiBase}/posts/${postId}`).pipe(
      tap(() => this._posts.update((posts) => posts.filter((p) => p.id !== postId))),
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

    return this.resolveAttachments(draft).pipe(
      switchMap((attachments) => {
        if (draft.kind === 'agenda' && draft.agenda) {
          return this.http
            .post<PostResponseDto>(`${this.feedApiBase}/posts/agenda`, {
              title: draft.agenda.title,
              eventDate: draft.agenda.date,
              location: draft.agenda.location,
              ...attachments,
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
                  ...attachments,
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
            ...attachments,
            visibility,
            context: visibility === 'private' ? 'Private' : 'Your feed',
          })
          .pipe(switchMap((dto) => this.tagAndReload(dto.id, 'text', visibility)));
      }),
    );
  }

  /** Casts (or changes) the signed-in account's vote and reflects the new tally locally —
   * matches the optimistic-update pattern `toggleLike` uses for the same reason (instant
   * feedback without waiting on a full post reload). */
  vote(postId: string, optionId: string): void {
    this.http.post<void>(`${this.feedApiBase}/posts/${postId}/poll/votes`, { optionId }).subscribe({
      next: () =>
        this._posts.update((posts) =>
          posts.map((p) => {
            if (p.id !== postId || !p.poll) {
              return p;
            }
            const previousOptionId = p.poll.myVoteOptionId;
            const options = p.poll.options.map((o) => {
              if (o.id === optionId) {
                return { ...o, votes: o.votes + 1 };
              }
              if (o.id === previousOptionId) {
                return { ...o, votes: Math.max(0, o.votes - 1) };
              }
              return o;
            });
            return { ...p, poll: { options, myVoteOptionId: optionId } };
          }),
        ),
    });
  }

  /** Uploads any image/file the composer attached, then hands back the plain fields the
   * create-post endpoints expect — untouched (all undefined) when the draft carries neither. */
  private resolveAttachments(draft: PostDraft): Observable<ResolvedAttachments> {
    const imageUpload$ = draft.imageFile ? this.uploadMedia(draft.imageFile) : of(undefined);
    const fileUpload$ = draft.attachedFile ? this.uploadMedia(draft.attachedFile) : of(undefined);
    return forkJoin([imageUpload$, fileUpload$]).pipe(
      map(([image, file]) => ({
        imageUrl: image?.url,
        fileUrl: file?.url,
        fileName: file?.fileName,
        pollOptions: draft.pollOptions && draft.pollOptions.length >= 2 ? draft.pollOptions : undefined,
      })),
    );
  }

  private uploadMedia(file: File): Observable<MediaUploadResponseDto> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<MediaUploadResponseDto>(`${this.feedApiBase}/media`, formData);
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
      metrics: this.http
        .get<PostMetricsResponseDto>(`${this.feedApiBase}/posts/${dto.id}/metrics`)
        .pipe(catchError(() => of({ likesCount: 0, commentsCount: 0 }))),
      liked: this.session.isAuthenticated()
        ? this.http
            .get<boolean>(`${this.feedApiBase}/posts/${dto.id}/likes/${this.session.account().id}`)
            .pipe(catchError(() => of(false)))
        : of(false),
      live: isLive
        ? this.http.get<LiveSessionResponseDto>(`${this.liveApiBase}/live-sessions/${dto.liveSessionId}`).pipe(catchError(() => of(null)))
        : of(null),
      myVoteOptionId:
        dto.pollOptions.length && this.session.isAuthenticated()
          ? this.http
              .get<string>(`${this.feedApiBase}/posts/${dto.id}/poll/votes/${this.session.account().id}`)
              .pipe(catchError(() => of(undefined)))
          : of(undefined),
    }).pipe(
      map(({ author, comments, metrics, liked, live, myVoteOptionId }): Post => {
        const tags: StatusTag[] = dto.tags.map((t) => ({
          label: t.label,
          severity: (t.severity ?? 'neutral') as TagSeverity,
          icon: t.icon ?? undefined,
        }));
        const pollOptions: PostPollOption[] = dto.pollOptions.map((o) => ({ id: o.id, label: o.label, votes: o.votes }));
        return {
          id: dto.id,
          author,
          createdAt: relativeTime(dto.createdAt),
          context: dto.context ?? '',
          content: dto.content ?? '',
          tags,
          kind: dto.kind as PostKind,
          imageUrl: dto.imageUrl ?? undefined,
          fileUrl: dto.fileUrl ?? undefined,
          fileName: dto.fileName ?? undefined,
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
          poll: pollOptions.length ? { options: pollOptions, myVoteOptionId: myVoteOptionId ?? undefined } : undefined,
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
