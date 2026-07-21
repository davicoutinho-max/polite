import { UserSummary } from './user.model';

export interface ChatMessage {
  readonly id: string;
  readonly fromMe: boolean;
  readonly text: string;
  readonly createdAt: string;
  readonly timeLabel: string;
  readonly edited: boolean;
  readonly deleted: boolean;
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
  readonly lastMessageDeleted: boolean;
  timeLabel: string;
  unread: number;
  readonly messages: ChatMessage[];
  /** Latest timestamp (ISO) at which any peer is known to have read this conversation — drives
   * the "Seen" label under my own last message, WhatsApp-style. Undefined = never confirmed read. */
  readonly peerReadAt?: string;
}
