import { computed, Injectable, signal } from '@angular/core';
import { Conversation, UserSummary } from '../models';

const AVATAR_A =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuDNF06tFrfgVfuEw_FOHQKoJQ-FGxIeD-WKKUf4bFAfJRNfHinpNj7IPKjXwJZ-BwhQhF5TBOJdOcahM6PA4rSbKCvV0Y9GUSm748-U1fFOS7Tv6AEJ-U6nJK75Cp_U9uPx1ebSN9gYtaN1t7AC4T7l2iXovmj25qvTxScJgZ0D0MVacpDHIs87kOvXrgibiMZj9zmtR_Oyed2kt01LUJlA5h_EHb7Yp1Ie1MVH0QLC5Bs06fXy4OuKpw';
const AVATAR_B =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuCE0D7JPXs4LxauFS-kbprWYD0-f7RD4Ydp-sfmuPS7GeKrwOzmWLMcM8So2XYtuMo0XRoKB7SSJjtsNMISN-k8Ir3lE5sh4D9A0hBEaXTEfegcl9xBAvm-Y1HJ9KR2mu2-pRJFtTe_dLVXrLZL89YvJipXpEpEMc0Yaz6ZnDIWEpRJ8_Z4xKTl6HEZocsNuZlqHHzZi2Lnvz37jInV5Ae79N_XeulYJMqQw8VN7FXRSeKD4Uvd5UoSaA';

@Injectable({ providedIn: 'root' })
export class MessagesService {
  private readonly _conversations = signal<Conversation[]>([
    {
      id: 'c1',
      participants: [{ id: 'jane-doe', name: 'Jane Doe', role: 'Federal Deputy', verified: true, avatarUrl: AVATAR_A }],
      isGroup: false,
      lastMessage: 'Thanks for supporting the water bill!',
      timeLabel: '2m',
      unread: 2,
      messages: [
        { id: 'm1', fromMe: false, text: 'Hi! Thanks for signing the transparency petition.', timeLabel: '09:40' },
        { id: 'm2', fromMe: true, text: 'Of course — it is an important cause.', timeLabel: '09:42' },
        { id: 'm3', fromMe: false, text: 'We reached the committee stage this morning.', timeLabel: '09:45' },
        { id: 'm4', fromMe: false, text: 'Thanks for supporting the water bill!', timeLabel: '09:46' },
      ],
    },
    {
      id: 'c2',
      participants: [{ id: 'marcus-chen', name: 'Rep. Marcus Chen', role: 'City Councilor', verified: true, avatarUrl: AVATAR_B }],
      isGroup: false,
      lastMessage: 'The transit report will be public on Friday.',
      timeLabel: '1h',
      unread: 0,
      messages: [
        { id: 'm1', fromMe: false, text: 'Have you seen the new transit numbers?', timeLabel: 'Yesterday' },
        { id: 'm2', fromMe: true, text: 'Not yet, when are they published?', timeLabel: 'Yesterday' },
        { id: 'm3', fromMe: false, text: 'The transit report will be public on Friday.', timeLabel: 'Yesterday' },
      ],
    },
    {
      id: 'c3',
      participants: [{ id: 'progressive', name: 'Progressive Party', role: 'Official channel', avatarUrl: AVATAR_A }],
      isGroup: false,
      lastMessage: 'Your affiliation request was approved 🎉',
      timeLabel: '3h',
      unread: 1,
      messages: [
        { id: 'm1', fromMe: false, text: 'Welcome! Your affiliation request was approved 🎉', timeLabel: '06:10' },
      ],
    },
  ]);
  readonly conversations = this._conversations.asReadonly();

  private readonly _activeId = signal<string>('c1');
  readonly activeId = this._activeId.asReadonly();

  readonly active = computed(() => this._conversations().find((c) => c.id === this._activeId()) ?? null);
  readonly totalUnread = computed(() => this._conversations().reduce((sum, c) => sum + c.unread, 0));

  select(id: string): void {
    this._activeId.set(id);
    this._conversations.update((list) => list.map((c) => (c.id === id ? { ...c, unread: 0 } : c)));
  }

  /** Starts a new conversation (1:1 or group) and makes it active. Returns its id. */
  createConversation(participants: UserSummary[], groupName?: string): string {
    const id = `c${Date.now()}`;
    const isGroup = participants.length > 1;
    const conversation: Conversation = {
      id,
      participants,
      isGroup,
      groupName: isGroup ? groupName?.trim() || participants.map((p) => p.name).join(', ') : undefined,
      lastMessage: '',
      timeLabel: 'now',
      unread: 0,
      messages: [],
    };
    this._conversations.update((list) => [conversation, ...list]);
    this._activeId.set(id);
    return id;
  }

  send(text: string): void {
    const body = text.trim();
    const id = this._activeId();
    if (!body) {
      return;
    }
    this._conversations.update((list) =>
      list.map((c) =>
        c.id === id
          ? {
              ...c,
              lastMessage: body,
              timeLabel: 'now',
              messages: [
                ...c.messages,
                { id: `m${Date.now()}`, fromMe: true, text: body, timeLabel: 'now' },
              ],
            }
          : c,
      ),
    );
  }
}
