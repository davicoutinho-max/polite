import { StatusTag } from './tag.model';
import { UserSummary } from './user.model';

export interface PostMetrics {
  likes: number;
  comments: number;
  liked: boolean;
}

export interface PostComment {
  readonly id: string;
  readonly author: UserSummary;
  readonly text: string;
  readonly timeLabel: string;
}

export type PostVisibility = 'public' | 'private';

/** Undefined/'text' behaves exactly like today's plain post. */
export type PostKind = 'text' | 'agenda' | 'live';

export interface PostAgenda {
  readonly title: string;
  /** Display label, e.g. "Aug 12, 2026 · 14:00". */
  readonly date: string;
  readonly location: string;
}

export interface PostLive {
  readonly videoId?: string;
  readonly channelId?: string;
  readonly isLiveNow: boolean;
  /** Shown when isLiveNow is false. */
  readonly scheduledFor?: string;
}

export interface PostPollOption {
  readonly id: string;
  readonly label: string;
  readonly votes: number;
}

export interface PostPoll {
  readonly options: PostPollOption[];
  /** The signed-in account's own vote, if any — undefined for visitors or accounts that haven't voted. */
  readonly myVoteOptionId?: string;
}

export interface Post {
  readonly id: string;
  readonly author: UserSummary;
  readonly createdAt: string;
  readonly context: string;
  readonly content: string;
  readonly tags: StatusTag[];
  readonly kind?: PostKind;
  readonly imageUrl?: string;
  /** A generic file attachment (pdf, doc, etc.), alongside/instead of an image. */
  readonly fileUrl?: string;
  readonly fileName?: string;
  /** Optional embedded YouTube video (on-demand clip). */
  readonly videoId?: string;
  /** Present iff kind === 'agenda'. */
  readonly agenda?: PostAgenda;
  /** Present iff kind === 'live'. */
  readonly live?: PostLive;
  /** Present iff the post carries a poll (2+ options) — the post's own content is the question. */
  readonly poll?: PostPoll;
  /** Whether the post is visible to everyone or kept private. */
  readonly visibility: PostVisibility;
  metrics: PostMetrics;
  comments: PostComment[];
}

export type FeedSort = 'top' | 'latest' | 'following';

/** A draft assembled by the post composer, ready for `FeedService.publish()`. */
export interface PostDraft {
  readonly kind: PostKind;
  readonly text: string;
  readonly visibility: PostVisibility;
  readonly agenda?: { title: string; date: string; location: string };
  readonly live?: { videoId: string; channelId: string; isLiveNow: boolean; scheduledFor: string };
  readonly imageFile?: File;
  readonly attachedFile?: File;
  /** 2+ entries attaches a poll to the post; fewer is ignored. */
  readonly pollOptions?: string[];
}
