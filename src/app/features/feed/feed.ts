import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { FeedService } from '../../core/services/feed.service';
import { TrendingService } from '../../core/services/trending.service';
import { BillsService } from '../../core/services/bills.service';
import { SessionService } from '../../core/services/session.service';
import { FeedSort as FeedSortValue, PostDraft } from '../../core/models';
import { PostComposer } from './components/post-composer/post-composer';
import { PostCard, CommentEvent } from './components/post-card/post-card';
import { FeedSort } from './components/feed-sort/feed-sort';
import { TrendingTopics } from './components/trending-topics/trending-topics';
import { RelevantBills } from './components/relevant-bills/relevant-bills';
import { LiveNow } from './components/live-now/live-now';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Feed page — orchestrates stores and lays out the two-column feed. */
@Component({
  selector: 'app-feed',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PostComposer, PostCard, FeedSort, TrendingTopics, RelevantBills, LiveNow, TranslatePipe],
  templateUrl: './feed.html',
  styleUrl: './feed.scss',
})
export class Feed {
  private readonly feedService = inject(FeedService);
  private readonly trendingService = inject(TrendingService);
  private readonly billsService = inject(BillsService);
  private readonly session = inject(SessionService);

  protected readonly currentUser = this.session.currentUser;
  /** Only politicians and parties may publish content. */
  protected readonly canPublish = computed(() => this.session.can('publish-content'));
  /** Citizens/politicians/parties may like and comment; visitors cannot. */
  protected readonly canReact = computed(() => this.session.can('react'));
  protected readonly posts = this.feedService.posts;
  protected readonly sort = this.feedService.sort;
  protected readonly topics = this.trendingService.topics;
  protected readonly bills = this.billsService.relevantBills;

  protected onPublish(draft: PostDraft): void {
    this.feedService.publish(draft).subscribe();
  }

  protected onLike(postId: string): void {
    this.feedService.toggleLike(postId);
  }

  protected onComment(event: CommentEvent): void {
    this.feedService.addComment(event.postId, event.text);
  }

  protected onSort(sort: FeedSortValue): void {
    this.feedService.setSort(sort);
  }
}
