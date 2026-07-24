import { ChangeDetectionStrategy, Component, computed, inject, input, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { Post, PostPoll, PostPollOption } from '../../../../core/models';
import { SessionService } from '../../../../core/services/session.service';
import { AlertsService } from '../../../../core/services/alerts.service';
import { DirectoryService } from '../../../../core/services/directory.service';
import { CompactNumberPipe } from '../../../../shared/pipes/compact-number.pipe';
import { UiAvatar } from '../../../../shared/ui/ui-avatar/ui-avatar';
import { UiCard } from '../../../../shared/ui/ui-card/ui-card';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { UiIconButton } from '../../../../shared/ui/ui-icon-button/ui-icon-button';
import { UiTag } from '../../../../shared/ui/ui-tag/ui-tag';
import { UiYoutube } from '../../../../shared/ui/ui-youtube/ui-youtube';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

export interface CommentEvent {
  readonly postId: string;
  readonly text: string;
}

export interface VoteEvent {
  readonly postId: string;
  readonly optionId: string;
}

/** Presentational feed post with expandable comments. State lives in the store. */
@Component({
  selector: 'app-post-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiCard, UiAvatar, UiIcon, UiTag, UiIconButton, CompactNumberPipe, RouterLink, FormsModule, InputText, UiYoutube, TranslatePipe],
  templateUrl: './post-card.html',
  styleUrl: './post-card.scss',
})
export class PostCard {
  private readonly session = inject(SessionService);
  private readonly alerts = inject(AlertsService);
  private readonly directory = inject(DirectoryService);

  readonly post = input.required<Post>();
  readonly currentUserAvatar = input('');
  /** When false, likes/comments are read-only (viewers without react rights). */
  readonly canReact = input(true);
  readonly like = output<string>();
  readonly addComment = output<CommentEvent>();
  readonly vote = output<VoteEvent>();
  readonly unvote = output<string>();
  readonly delete = output<string>();

  protected readonly showComments = signal(false);
  protected readonly showMenu = signal(false);
  protected readonly draft = signal('');

  protected readonly canDelete = computed(() => this.session.account().id === this.post().author.id);

  /** Politicians get a /profile page, parties get a /party page — both share the same accounts
   * table so nothing on the author itself distinguishes them; this checks DirectoryService's
   * already-loaded party list instead. Routing every author to /profile regardless of type used
   * to silently 404/error the party's dossier lookup on the profile page, which then just kept
   * showing whatever politician had loaded there previously. */
  protected readonly authorLink = computed<string[]>(() => {
    const id = this.post().author.id;
    return this.directory.parties().some((p) => p.id === id) ? ['/party', id] : ['/profile', id];
  });

  protected toggleMenu(): void {
    this.showMenu.update((v) => !v);
  }

  protected onDelete(): void {
    this.showMenu.set(false);
    this.delete.emit(this.post().id);
  }

  protected share(): void {
    const url = `${location.origin}${this.authorLink().join('/')}`;
    const shareData = { title: this.post().author.name, text: this.post().content, url };
    if (navigator.share) {
      navigator.share(shareData).catch(() => undefined);
      return;
    }
    navigator.clipboard
      .writeText(url)
      .then(() =>
        this.alerts.push({
          category: 'project',
          icon: 'link',
          title: 'Link copied',
          message: 'A link to this post was copied to your clipboard.',
          timeLabel: 'Just now',
        }),
      )
      .catch(() => undefined);
  }

  protected toggleComments(): void {
    this.showComments.update((v) => !v);
  }

  protected submitComment(): void {
    const text = this.draft().trim();
    if (!text) {
      return;
    }
    this.addComment.emit({ postId: this.post().id, text });
    this.draft.set('');
  }

  protected pollTotalVotes(poll: PostPoll): number {
    return poll.options.reduce((sum, o) => sum + o.votes, 0);
  }

  protected pollPercentage(option: PostPollOption, poll: PostPoll): number {
    const total = this.pollTotalVotes(poll);
    return total === 0 ? 0 : Math.round((option.votes / total) * 100);
  }

  /** A poll is never "definitive" while open — clicking your own vote again withdraws it,
   * clicking another option switches it. Both are blocked once the poll's optional deadline has
   * passed. */
  protected isPollClosed(poll: PostPoll): boolean {
    return !!poll.closesAt && new Date(poll.closesAt).getTime() <= Date.now();
  }

  protected onPollOptionClick(poll: PostPoll, option: PostPollOption): void {
    if (this.isPollClosed(poll)) {
      return;
    }
    if (poll.myVoteOptionId === option.id) {
      this.unvote.emit(this.post().id);
    } else {
      this.vote.emit({ postId: this.post().id, optionId: option.id });
    }
  }
}
