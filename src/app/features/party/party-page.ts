import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PartyService } from '../../core/services/party.service';
import { DirectoryService } from '../../core/services/directory.service';
import { FeedService } from '../../core/services/feed.service';
import { SessionService } from '../../core/services/session.service';
import { ProfileTab } from '../../core/models';
import { UiSection } from '../../shared/ui/ui-section/ui-section';
import { UiStat } from '../../shared/ui/ui-stat/ui-stat';
import { DataListItem, UiDataList } from '../../shared/ui/ui-data-list/ui-data-list';
import { UiTag } from '../../shared/ui/ui-tag/ui-tag';
import { UiAvatar } from '../../shared/ui/ui-avatar/ui-avatar';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiEmpty } from '../../shared/ui/ui-empty/ui-empty';
import { ProfileTabs } from '../profile/components/profile-tabs/profile-tabs';
import { PostCard, CommentEvent } from '../feed/components/post-card/post-card';
import { TranslateService } from '../../core/services/translate.service';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

const TABS: ProfileTab[] = [
  { id: 'activity', label: 'Activity', key: 'tab.activity', icon: 'forum' },
  { id: 'overview', label: 'Overview', key: 'tab.overview', icon: 'info' },
];

/** Party profile page. */
@Component({
  selector: 'app-party-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    UiSection,
    UiStat,
    UiDataList,
    UiTag,
    UiAvatar,
    UiButton,
    UiIcon,
    UiEmpty,
    RouterLink,
    ProfileTabs,
    PostCard,
    TranslatePipe,
  ],
  templateUrl: './party-page.html',
  styleUrl: './party-page.scss',
})
export class PartyPage {
  private readonly partyService = inject(PartyService);
  private readonly directory = inject(DirectoryService);
  private readonly feedService = inject(FeedService);
  private readonly session = inject(SessionService);
  private readonly translate = inject(TranslateService);
  private readonly route = inject(ActivatedRoute);

  protected readonly party = this.partyService.party;
  protected readonly following = computed(() => this.directory.isFollowing('party', this.party().id));

  protected readonly tabs = TABS;
  protected readonly activeTab = signal('activity');

  protected readonly activityPosts = computed(() => this.feedService.postsByAuthor(this.party().id)());
  protected readonly canReact = computed(() => this.session.can('react'));
  protected readonly currentUserAvatar = computed(() => this.session.currentUser().avatarUrl);

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.partyService.load(id).subscribe();
      }
    });
  }

  protected setActiveTab(id: string): void {
    this.activeTab.set(id);
  }

  protected toggleFollow(): void {
    const id = this.party().id;
    if (this.following()) {
      this.directory.unfollow('party', id).subscribe();
    } else {
      this.directory.follow('party', id).subscribe();
    }
  }

  protected onLike(postId: string): void {
    this.feedService.toggleLike(postId);
  }

  protected onComment(event: CommentEvent): void {
    this.feedService.addComment(event.postId, event.text);
  }

  protected readonly hasStatute = computed(() => !!this.party().statuteUrl && this.party().statuteUrl !== '#');

  protected openStatute(): void {
    const url = this.party().statuteUrl;
    if (url && url !== '#') {
      window.open(url, '_blank', 'noopener');
    }
  }

  protected readonly overview = computed<DataListItem[]>(() => {
    const p = this.party();
    const t = (key: string, fallback: string) => this.translate.t(key, fallback);
    return [
      { icon: 'tag', label: t('label.number', 'Number'), value: String(p.number) },
      { icon: 'psychology', label: t('label.ideology', 'Ideology'), value: p.ideology },
      { icon: 'event', label: t('label.founded', 'Founded'), value: String(p.foundedYear) },
      { icon: 'person', label: t('label.president', 'President'), value: p.president },
    ];
  });
}
