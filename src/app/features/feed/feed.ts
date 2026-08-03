import { ChangeDetectionStrategy, Component, computed, inject, viewChild } from '@angular/core';
import { FeedService } from '../../core/services/feed.service';
import { TrendingService } from '../../core/services/trending.service';
import { BillsService } from '../../core/services/bills.service';
import { SessionService } from '../../core/services/session.service';
import { AlertsService } from '../../core/services/alerts.service';
import { TranslateService } from '../../core/services/translate.service';
import { PartyService } from '../../core/services/party.service';
import { SocialConnectionService, SocialPlatform } from '../../core/services/social-connection.service';
import { FeedSort as FeedSortValue, PostDraft } from '../../core/models';
import { InfiniteScrollDirective } from '../../core/directives/infinite-scroll.directive';
import { PostComposer } from './components/post-composer/post-composer';
import { PostCard, CommentEvent, VoteEvent } from './components/post-card/post-card';
import { FeedSort } from './components/feed-sort/feed-sort';
import { TrendingTopics } from './components/trending-topics/trending-topics';
import { RelevantBills } from './components/relevant-bills/relevant-bills';
import { LiveNow } from './components/live-now/live-now';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiCard } from '../../shared/ui/ui-card/ui-card';
import { UiSkeleton } from '../../shared/ui/ui-skeleton/ui-skeleton';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Feed page — orchestrates stores and lays out the two-column feed. */
@Component({
  selector: 'app-feed',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PostComposer,
    PostCard,
    FeedSort,
    TrendingTopics,
    RelevantBills,
    LiveNow,
    InfiniteScrollDirective,
    UiIcon,
    UiCard,
    UiSkeleton,
    TranslatePipe,
  ],
  templateUrl: './feed.html',
  styleUrl: './feed.scss',
})
export class Feed {
  private readonly feedService = inject(FeedService);
  private readonly trendingService = inject(TrendingService);
  private readonly billsService = inject(BillsService);
  private readonly session = inject(SessionService);
  private readonly alerts = inject(AlertsService);
  private readonly translate = inject(TranslateService);
  private readonly socialConnections = inject(SocialConnectionService);
  private readonly partyService = inject(PartyService);
  private readonly composer = viewChild(PostComposer);

  protected readonly currentUser = this.session.currentUser;
  /** Only politicians and parties may publish content. */
  protected readonly canPublish = computed(() => this.session.can('publish-content'));
  /** Citizens/politicians/parties may like and comment; visitors cannot. */
  protected readonly canReact = computed(() => this.session.can('react'));
  protected readonly posts = this.feedService.posts;
  protected readonly loading = this.feedService.loading;
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
      next: (post) => {
        composer?.onPublishSucceeded();
        if (post && draft.socialPlatforms && draft.socialPlatforms.length > 0) {
          this.publishToSocialNetworks(post.id, draft.socialPlatforms as SocialPlatform[]);
        }
        if (draft.kind === 'agenda' && draft.agenda) {
          this.mirrorAgendaAsPartyEvent(draft.agenda);
        }
      },
      error: () => composer?.onPublishFailed('Could not publish your post. Please try again.'),
    });
  }

  /** A party's "agenda" post is also a real scheduled event — mirroring it into
   * party-management-service means it shows up in that party's own Agenda tab (while upcoming)
   * and Events tab (once its date passes), the same split-by-date view admin-created events
   * already get (see PartyPage's upcomingEvents/pastEvents). Only parties have an events list at
   * all, so this is a no-op for politician-authored agenda posts. Best-effort: the post itself
   * already succeeded by the time this runs, so a failure here is silent rather than rolled back. */
  private mirrorAgendaAsPartyEvent(agenda: { title: string; date: string; location: string }): void {
    const account = this.session.account();
    if (account.accountType !== 'party') {
      return;
    }
    const eventDate = agenda.date.split('T')[0];
    this.partyService
      .createEvent(agenda.title, eventDate, agenda.location, this.translate.t('tab.agenda', 'Agenda'), 'secondary', account.id)
      .subscribe({ error: () => undefined });
  }

  /** Best-effort — the in-app post already succeeded by the time this runs, so a cross-posting
   * failure here is surfaced as a toast, never rolled back or blocked on. */
  private publishToSocialNetworks(postId: string, platforms: SocialPlatform[]): void {
    this.socialConnections.publish(postId, platforms).subscribe({
      next: (shares) => {
        const failed = shares.filter((s) => s.status === 'failed');
        if (failed.length === 0) {
          this.alerts.push({
            category: 'project',
            icon: 'check_circle',
            title: this.translate.t('title.social-share-success', 'Shared!'),
            message: shares.map((s) => s.platform).join(', '),
            timeLabel: this.translate.t('label.just-now', 'Just now'),
          });
        } else {
          this.alerts.push({
            category: 'project',
            icon: 'error',
            title: this.translate.t('title.social-share-partial', 'Some networks failed'),
            message: failed.map((s) => `${s.platform}: ${s.errorMessage}`).join(' · '),
            timeLabel: this.translate.t('label.just-now', 'Just now'),
          });
        }
      },
      error: () =>
        this.alerts.push({
          category: 'project',
          icon: 'error',
          title: this.translate.t('title.social-share-failed', 'Sharing failed'),
          message: this.translate.t('hint.social-share-failed', 'Could not share this post to the selected networks.'),
          timeLabel: this.translate.t('label.just-now', 'Just now'),
        }),
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
