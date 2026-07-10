import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Post } from '../../../../core/models';
import { CompactNumberPipe } from '../../../../shared/pipes/compact-number.pipe';
import { UiAvatar } from '../../../../shared/ui/ui-avatar/ui-avatar';
import { UiCard } from '../../../../shared/ui/ui-card/ui-card';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { UiIconButton } from '../../../../shared/ui/ui-icon-button/ui-icon-button';
import { UiTag } from '../../../../shared/ui/ui-tag/ui-tag';
import { UiYoutube } from '../../../../shared/ui/ui-youtube/ui-youtube';

export interface CommentEvent {
  readonly postId: string;
  readonly text: string;
}

/** Presentational feed post with expandable comments. State lives in the store. */
@Component({
  selector: 'app-post-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiCard, UiAvatar, UiIcon, UiTag, UiIconButton, CompactNumberPipe, RouterLink, FormsModule, UiYoutube],
  templateUrl: './post-card.html',
  styleUrl: './post-card.scss',
})
export class PostCard {
  readonly post = input.required<Post>();
  readonly currentUserAvatar = input('');
  /** When false, likes/comments are read-only (viewers without react rights). */
  readonly canReact = input(true);
  readonly like = output<string>();
  readonly addComment = output<CommentEvent>();

  protected readonly showComments = signal(false);
  protected readonly draft = signal('');

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
}
