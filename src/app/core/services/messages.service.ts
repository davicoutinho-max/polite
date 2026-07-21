import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { Observable, catchError, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { ChatMessage, Conversation, UserSummary } from '../models';
import { relativeTime } from '../utils/relative-time';
import { DirectoryService } from './directory.service';
import { SessionService } from './session.service';

interface ConversationResponseDto {
  readonly id: string;
  readonly group: boolean;
  readonly groupName: string | null;
  readonly groupAvatarUrl: string | null;
  readonly lastMessageAt: string | null;
}

interface ParticipantResponseDto {
  readonly accountId: string;
  readonly lastReadAt: string | null;
}

interface MessageResponseDto {
  readonly id: string;
  readonly senderAccountId: string;
  readonly body: string | null;
  readonly createdAt: string;
  readonly editedAt: string | null;
  readonly deleted: boolean;
}

interface AccountResponseDto {
  readonly id: string;
  readonly name: string;
  readonly handle: string;
  readonly avatarUrl: string;
  readonly verified: boolean;
}

interface MediaUploadResponseDto {
  readonly url: string;
  readonly fileName: string;
}

@Injectable({ providedIn: 'root' })
export class MessagesService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly directory = inject(DirectoryService);
  private readonly apiBase = `${environment.apiBaseUrl}/api/messaging`;
  private readonly identityApiBase = `${environment.apiBaseUrl}/api/identity`;
  /** No dedicated upload endpoint lives on messaging-service — feed-content-service's is
   * generic (any file, not post-specific) so group photos reuse it rather than duplicating
   * upload/storage plumbing here. */
  private readonly mediaApiBase = `${environment.apiBaseUrl}/api/feed`;

  private readonly participantCache = new Map<string, UserSummary>();

  private readonly _conversations = signal<Conversation[]>([]);
  readonly conversations = this._conversations.asReadonly();

  private readonly _activeId = signal<string | null>(null);
  readonly activeId = this._activeId.asReadonly();

  readonly active = computed(() => this._conversations().find((c) => c.id === this._activeId()) ?? null);
  readonly totalUnread = computed(() => this._conversations().reduce((sum, c) => sum + c.unread, 0));

  // ---- Real-time (STOMP over WebSocket): live message push, typing, read receipts ----
  private stompClient: Client | null = null;
  private readonly subscribedConversationIds = new Set<string>();
  private readonly typingTimers = new Map<string, ReturnType<typeof setTimeout>>();
  private readonly _typingByConversation = signal<Record<string, ReadonlySet<string>>>({});
  readonly typingByConversation = this._typingByConversation.asReadonly();

  constructor() {
    this.reload().subscribe(() => this.connectRealtime());
  }

  reload(): Observable<Conversation[]> {
    if (!this.session.isAuthenticated()) {
      return of([]);
    }
    return this.http.get<ConversationResponseDto[]>(`${this.apiBase}/conversations`).pipe(
      switchMap((list) => (list.length ? forkJoin(list.map((dto) => this.toConversation(dto))) : of([]))),
      tap((conversations) => this._conversations.set(conversations)),
    );
  }

  select(id: string): void {
    this._activeId.set(id);
    this.http.post(`${this.apiBase}/conversations/${id}/read`, {}).subscribe({
      next: () => this._conversations.update((list) => list.map((c) => (c.id === id ? { ...c, unread: 0 } : c))),
    });
  }

  /** Starts a new conversation (1:1 or group) and makes it active. */
  createConversation(participants: UserSummary[], groupName?: string): void {
    const isGroup = participants.length > 1;
    const request$ = isGroup
      ? this.http.post<ConversationResponseDto>(`${this.apiBase}/conversations/group`, {
          participantAccountIds: participants.map((p) => p.id),
          groupName: groupName?.trim() || participants.map((p) => p.name).join(', '),
          groupAvatarUrl: null,
        })
      : this.http.post<ConversationResponseDto>(`${this.apiBase}/conversations/direct`, { otherAccountId: participants[0]?.id });

    request$.pipe(switchMap((dto) => this.toConversation(dto))).subscribe({
      next: (conversation) => {
        this._conversations.update((list) => [conversation, ...list]);
        this._activeId.set(conversation.id);
        this.ensureSubscribed(conversation.id);
      },
    });
  }

  send(text: string): void {
    const body = text.trim();
    const id = this._activeId();
    if (!body || !id) {
      return;
    }
    this.http.post<MessageResponseDto>(`${this.apiBase}/conversations/${id}/messages`, { body }).subscribe({
      next: (dto) => this.applyIncomingMessage(id, dto),
    });
  }

  /** Only the original sender may edit their own message — enforced server-side too. */
  editMessage(conversationId: string, messageId: string, newText: string): void {
    const body = newText.trim();
    if (!body) {
      return;
    }
    this.http.put<MessageResponseDto>(`${this.apiBase}/conversations/${conversationId}/messages/${messageId}`, { body }).subscribe({
      next: (dto) => this.applyIncomingMessage(conversationId, dto),
    });
  }

  /** Soft delete — the server clears the body; the UI renders a "deleted" tombstone in its place. */
  deleteMessage(conversationId: string, messageId: string): void {
    this.http.delete<MessageResponseDto>(`${this.apiBase}/conversations/${conversationId}/messages/${messageId}`).subscribe({
      next: (dto) => this.applyIncomingMessage(conversationId, dto),
    });
  }

  /** Fire-and-forget "I'm typing" ping — the composer already throttles calls to this method. */
  sendTyping(conversationId: string): void {
    if (this.stompClient?.connected) {
      this.stompClient.publish({ destination: `/app/conversations/${conversationId}/typing`, body: '' });
    }
  }

  /** Any participant may rename a group — there's no separate "admin" role in this model. */
  renameGroup(conversationId: string, newName: string): void {
    const groupName = newName.trim();
    if (!groupName) {
      return;
    }
    this.http.put<ConversationResponseDto>(`${this.apiBase}/conversations/${conversationId}/group-name`, { groupName }).subscribe({
      next: (dto) => this.patchConversation(conversationId, { groupName: dto.groupName ?? undefined }),
    });
  }

  changeGroupAvatar(conversationId: string, groupAvatarUrl: string | null): void {
    this.http.put<ConversationResponseDto>(`${this.apiBase}/conversations/${conversationId}/group-avatar`, { groupAvatarUrl }).subscribe({
      next: (dto) => this.patchConversation(conversationId, { groupAvatarUrl: dto.groupAvatarUrl ?? undefined }),
    });
  }

  /** Uploads the picked file (via feed-content-service's generic media endpoint), then sets the
   * resulting URL as the group's avatar. */
  changeGroupAvatarFile(conversationId: string, file: File): void {
    const formData = new FormData();
    formData.append('file', file);
    this.http
      .post<MediaUploadResponseDto>(`${this.mediaApiBase}/media`, formData)
      .pipe(switchMap((upload) => this.http.put<ConversationResponseDto>(`${this.apiBase}/conversations/${conversationId}/group-avatar`, {
        groupAvatarUrl: upload.url,
      })))
      .subscribe({
        next: (dto) => this.patchConversation(conversationId, { groupAvatarUrl: dto.groupAvatarUrl ?? undefined }),
      });
  }

  private toChatMessage(m: MessageResponseDto, myId: string | null): ChatMessage {
    return {
      id: m.id,
      fromMe: m.senderAccountId === myId,
      text: m.body ?? '',
      createdAt: m.createdAt,
      timeLabel: relativeTime(m.createdAt),
      edited: !!m.editedAt,
      deleted: m.deleted,
    };
  }

  /** Append-or-replace by message id — the single path for send/edit/delete's own HTTP response
   * AND the WebSocket broadcast of the same event, so whichever arrives first "wins" and the
   * other is a harmless no-op update rather than a duplicate bubble. */
  private applyIncomingMessage(conversationId: string, dto: MessageResponseDto): void {
    const myId = this.session.isAuthenticated() ? this.session.account().id : null;
    const message = this.toChatMessage(dto, myId);
    this._conversations.update((list) =>
      list.map((c): Conversation => {
        if (c.id !== conversationId) {
          return c;
        }
        const existed = c.messages.some((m) => m.id === message.id);
        const messages = existed ? c.messages.map((m) => (m.id === message.id ? message : m)) : [...c.messages, message];
        const isLast = messages.at(-1)?.id === message.id;
        const isActive = this._activeId() === conversationId;
        if (!existed && !message.fromMe && isActive) {
          this.http.post(`${this.apiBase}/conversations/${conversationId}/read`, {}).subscribe();
        }
        const unread = !existed && !message.fromMe && !isActive ? c.unread + 1 : c.unread;
        return isLast
          ? { ...c, messages, unread, lastMessage: message.deleted ? '' : message.text, lastMessageDeleted: message.deleted, timeLabel: relativeTime(dto.createdAt) }
          : { ...c, messages, unread };
      }),
    );
  }

  private applyReadReceipt(conversationId: string, accountId: string, readAt: string): void {
    const myId = this.session.isAuthenticated() ? this.session.account().id : null;
    if (accountId === myId) {
      return;
    }
    this._conversations.update((list) => list.map((c) => (c.id === conversationId ? { ...c, peerReadAt: readAt } : c)));
  }

  private patchConversation(conversationId: string, patch: Partial<Pick<Conversation, 'groupName' | 'groupAvatarUrl'>>): void {
    this._conversations.update((list) => list.map((c) => (c.id === conversationId ? { ...c, ...patch } : c)));
  }

  // ---- Real-time wiring ----

  private connectRealtime(): void {
    if (this.stompClient || !this.session.isAuthenticated()) {
      return;
    }
    const token = this.session.accessToken();
    if (!token) {
      return;
    }
    const wsBase = environment.apiBaseUrl.replace(/^http/, 'ws');
    const client = new Client({
      brokerURL: `${wsBase}/api/messaging/ws?token=${encodeURIComponent(token)}`,
      reconnectDelay: 3000,
      onConnect: () => {
        this.subscribedConversationIds.clear();
        this._conversations().forEach((c) => this.ensureSubscribed(c.id));
      },
    });
    client.activate();
    this.stompClient = client;
  }

  private ensureSubscribed(conversationId: string): void {
    if (!this.stompClient?.connected || this.subscribedConversationIds.has(conversationId)) {
      return;
    }
    this.subscribedConversationIds.add(conversationId);
    this.stompClient.subscribe(`/topic/conversations/${conversationId}`, (frame) => this.handleMessageFrame(conversationId, frame));
    this.stompClient.subscribe(`/topic/conversations/${conversationId}/typing`, (frame) => this.handleTypingFrame(conversationId, frame));
  }

  private handleMessageFrame(conversationId: string, frame: IMessage): void {
    const payload = JSON.parse(frame.body) as { type: 'message' | 'read'; [key: string]: unknown };
    if (payload['type'] === 'message') {
      this.applyIncomingMessage(conversationId, payload as unknown as MessageResponseDto);
    } else if (payload['type'] === 'read') {
      this.applyReadReceipt(conversationId, payload['accountId'] as string, payload['readAt'] as string);
    }
  }

  private handleTypingFrame(conversationId: string, frame: IMessage): void {
    const payload = JSON.parse(frame.body) as { accountId: string };
    const myId = this.session.isAuthenticated() ? this.session.account().id : null;
    if (payload.accountId === myId) {
      return;
    }
    const timerKey = `${conversationId}:${payload.accountId}`;
    this._typingByConversation.update((map) => {
      const next = new Set(map[conversationId] ?? []);
      next.add(payload.accountId);
      return { ...map, [conversationId]: next };
    });
    clearTimeout(this.typingTimers.get(timerKey));
    this.typingTimers.set(
      timerKey,
      setTimeout(() => {
        this._typingByConversation.update((map) => {
          const next = new Set(map[conversationId] ?? []);
          next.delete(payload.accountId);
          return { ...map, [conversationId]: next };
        });
      }, 3000),
    );
  }

  private toConversation(dto: ConversationResponseDto): Observable<Conversation> {
    const myId = this.session.isAuthenticated() ? this.session.account().id : null;
    return forkJoin({
      participants: this.http.get<ParticipantResponseDto[]>(`${this.apiBase}/conversations/${dto.id}/participants`),
      messages: this.http.get<MessageResponseDto[]>(`${this.apiBase}/conversations/${dto.id}/messages`, { params: { pageSize: 50 } }),
    }).pipe(
      switchMap(({ participants, messages }) => {
        const others = participants.filter((p) => p.accountId !== myId);
        const mine = participants.find((p) => p.accountId === myId);
        const lastReadAt = mine?.lastReadAt ? new Date(mine.lastReadAt).getTime() : 0;
        const unread = messages.filter((m) => m.senderAccountId !== myId && new Date(m.createdAt).getTime() > lastReadAt).length;
        const peerReadAt = others.reduce<string | undefined>(
          (max, p) => (p.lastReadAt && (!max || p.lastReadAt > max) ? p.lastReadAt : max),
          undefined,
        );

        const participants$: Observable<UserSummary[]> = others.length
          ? forkJoin(others.map((p) => this.resolveParticipant(p.accountId)))
          : of([]);

        return participants$.pipe(
          map((participantSummaries): Conversation => {
            const lastMessage = messages[messages.length - 1];
            return {
              id: dto.id,
              participants: participantSummaries,
              isGroup: dto.group,
              groupName: dto.groupName ?? undefined,
              groupAvatarUrl: dto.groupAvatarUrl ?? undefined,
              lastMessage: lastMessage?.body ?? '',
              lastMessageDeleted: !!lastMessage?.deleted,
              timeLabel: dto.lastMessageAt ? relativeTime(dto.lastMessageAt) : '',
              unread,
              messages: messages.map((m): ChatMessage => this.toChatMessage(m, myId)),
              peerReadAt,
            };
          }),
        );
      }),
    );
  }

  private resolveParticipant(accountId: string): Observable<UserSummary> {
    const cached = this.participantCache.get(accountId);
    if (cached) {
      return of(cached);
    }
    const politician = this.directory.politicians().find((p) => p.id === accountId);
    if (politician) {
      const summary: UserSummary = {
        id: politician.id,
        name: politician.name,
        handle: politician.handle,
        avatarUrl: politician.avatarUrl,
        verified: politician.verified,
        role: politician.office,
      };
      this.participantCache.set(accountId, summary);
      return of(summary);
    }
    const party = this.directory.parties().find((p) => p.id === accountId);
    if (party) {
      const summary: UserSummary = { id: party.id, name: party.name, avatarUrl: party.logoUrl, verified: true, role: 'Official channel' };
      this.participantCache.set(accountId, summary);
      return of(summary);
    }
    return this.http.get<AccountResponseDto>(`${this.identityApiBase}/accounts/${accountId}`).pipe(
      map(
        (r): UserSummary => ({ id: r.id, name: r.name, handle: r.handle, avatarUrl: r.avatarUrl, verified: r.verified }),
      ),
      tap((summary) => this.participantCache.set(accountId, summary)),
      catchError(() => of({ id: accountId, name: 'Unknown', avatarUrl: '', verified: false })),
    );
  }
}
