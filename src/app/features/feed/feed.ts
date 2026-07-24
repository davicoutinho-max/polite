import { ChangeDetectionStrategy, Component, computed, inject, viewChild } from '@angular/core';
import { FeedService } from '../../core/services/feed.service';
import { TrendingService } from '../../core/services/trending.service';
import { BillsService } from '../../core/services/bills.service';
import { SessionService } from '../../core/services/session.service';
import { FeedSort as FeedSortValue, PostDraft } from '../../core/models';
import { InfiniteScrollDirective } from '../../core/directives/infinite-scroll.directive';
import { PostComposer } from './components/post-composer/post-composer';
import { PostCard, CommentEvent, VoteEvent } from './components/post-card/post-card';
import { FeedSort } from './components/feed-sort/feed-sort';
import { TrendingTopics } from './components/trending-topics/trending-topics';
import { RelevantBills } from './components/relevant-bills/relevant-bills';
import { LiveNow } from './components/live-now/live-now';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Feed page — orchestrates stores and lays out the two-column feed. */
@Component({
  selector: 'app-feed',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PostComposer, PostCard, FeedSort, TrendingTopics, RelevantBills, LiveNow, InfiniteScrollDirective, UiIcon, TranslatePipe],
  templateUrl: './feed.html',
  styleUrl: './feed.scss',
})
export class Feed {
  private readonly feedService = inject(FeedService);
  private readonly trendingService = inject(TrendingService);
  private readonly billsService = inject(BillsService);
  private readonly session = inject(SessionService);
  private readonly composer = viewChild(PostComposer);

  protected readonly currentUser = this.session.currentUser;
  /** Only politicians and parties may publish content. */
  protected readonly canPublish = computed(() => this.session.can('publish-content'));
  /** Citizens/politicians/parties may like and comment; visitors cannot. */
  protected readonly canReact = computed(() => this.session.can('react'));
  protected readonly posts = this.feedService.posts;
  protected readonly sort = this.feedService.sort;
  protected readonly topics = this.trendingService.topics;
  protected readonly bills = this.billsService.relevantBills;
  protected readonly hasMore = this.feedService.hasMore;

  protected onLoadMore(): void {
    this.feedService.loadMore();
  }

  protected onPublish(draft: PostDraft): void {
    const composer = this.composer();
    composer?.markSubmitting();
    this.feedService.publish(draft).subscribe({
      next: () => composer?.onPublishSucceeded(),
      error: () => composer?.onPublishFailed('Could not publish your post. Please try again.'),
    });
  }

  protected onLike(postId: string): void {
    this.feedService.toggleLike(postId);
  }

  protected onComment(event: CommentEvent): void {
    this.feedService.addComment(event.postId, event.text);
  }

  protected onVote(event: VoteEvent): void {
    this.feedService.vote(event.postId, event.optionId);
  }

  protected onUnvote(postId: string): void {
    this.feedService.unvote(postId);
  }

  protected onDelete(postId: string): void {
    this.feedService.deletePost(postId).subscribe({ error: () => undefined });
  }

  protected onSort(sort: FeedSortValue): void {
    this.feedService.setSort(sort);
  }
}
