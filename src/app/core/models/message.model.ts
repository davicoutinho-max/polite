import { UserSummary } from './user.model';

export interface ChatMessage {
  readonly id: string;
  readonly fromMe: boolean;
  readonly text: string;
  readonly timeLabel: string;
}

export interface Conversation {
  readonly id: string;
  /** 1 entry = a regular 1:1 conversation, 2+ = a group. */
  readonly participants: UserSummary[];
  readonly isGroup: boolean;
  /** Set for groups only; falls back to joined participant names when absent. */
  readonly groupName?: string;
  readonly groupAvatarUrl?: string;
  lastMessage: string;
  timeLabel: string;
  unread: number;
  readonly messages: ChatMessage[];
}
