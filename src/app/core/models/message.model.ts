import { UserSummary } from './user.model';

export type MessageAttachmentType = 'image' | 'video' | 'audio' | 'file';

export interface ChatMessage {
  readonly id: string;
  readonly fromMe: boolean;
  readonly text: string;
  readonly createdAt: string;
  readonly timeLabel: string;
  readonly edited: boolean;
  readonly deleted: boolean;
  readonly attachmentUrl?: string;
  readonly attachmentType?: MessageAttachmentType;
  readonly attachmentFileName?: string;
  /** Id of the message this one quotes, if any — resolve the preview against the conversation's
   * already-loaded `messages` list (see messages-page.ts's `replyPreview`). */
  readonly replyToMessageId?: string;
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
  /** Whether an older page of message history is worth requesting — false once a fetch comes
   * back short of a full page. Drives the "load older" infinite-scroll sentinel at the top of
   * the thread. */
  readonly hasMoreHistory: boolean;
}
