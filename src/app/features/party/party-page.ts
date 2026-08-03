import { ChangeDetectionStrategy, Component, computed, ElementRef, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PartyService } from '../../core/services/party.service';
import { DirectoryService } from '../../core/services/directory.service';
import { FeedService } from '../../core/services/feed.service';
import { MediaService } from '../../core/services/media.service';
import { SessionService } from '../../core/services/session.service';
import { ProfileTab } from '../../core/models';
import { UiSection } from '../../shared/ui/ui-section/ui-section';
import { StatStripItem, UiStatStrip } from '../../shared/ui/ui-stat-strip/ui-stat-strip';
import { DataListItem, UiDataList } from '../../shared/ui/ui-data-list/ui-data-list';
import { UiTag } from '../../shared/ui/ui-tag/ui-tag';
import { UiAvatar } from '../../shared/ui/ui-avatar/ui-avatar';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiEmpty } from '../../shared/ui/ui-empty/ui-empty';
import { UiSkeleton } from '../../shared/ui/ui-skeleton/ui-skeleton';
import { ProfileTabs } from '../profile/components/profile-tabs/profile-tabs';
import { PostCard, CommentEvent, VoteEvent } from '../feed/components/post-card/post-card';
import { TranslateService } from '../../core/services/translate.service';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { UiYoutube } from '../../shared/ui/ui-youtube/ui-youtube';
import { extractYouTubeId } from '../../shared/utils/video-url';

const TABS: ProfileTab[] = [
  { id: 'activity', label: 'Activity', key: 'tab.activity', icon: 'forum' },
  { id: 'overview', label: 'Overview', key: 'tab.overview', icon: 'info' },
  { id: 'agenda', label: 'Agenda', key: 'tab.agenda', icon: 'calendar_month' },
  { id: 'events', label: 'Events', key: 'tab.events', icon: 'event' },
];

/** Party profile page. */
@Component({
  selector: 'app-party-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    UiSection,
    UiStatStrip,
    UiDataList,
    UiTag,
    UiAvatar,
    UiButton,
    UiIcon,
    UiEmpty,
    UiSkeleton,
    RouterLink,
    ProfileTabs,
    PostCard,
    TranslatePipe,
    UiYoutube,
  ],
  templateUrl: './party-page.html',
  styleUrl: './party-page.scss',
})
export class PartyPage {
  private readonly partyService = inject(PartyService);
  private readonly directory = inject(DirectoryService);
  private readonly feedService = inject(FeedService);
  private readonly media = inject(MediaService);
  private readonly session = inject(SessionService);
  private readonly translate = inject(TranslateService);
  private readonly route = inject(ActivatedRoute);

  protected readonly party = this.partyService.party;
  protected readonly loading = this.partyService.loading;
  protected readonly following = computed(() => this.directory.isFollowing('party', this.party().id));

  protected readonly tabs = TABS;
  protected readonly activeTab = signal('activity');

  protected readonly activityPosts = computed(() => this.feedService.postsByAuthor(this.party().id)());
  protected readonly canReact = computed(() => this.session.can('react'));
  protected readonly currentUserAvatar = computed(() => this.session.currentUser().avatarUrl);
  protected readonly canFollow = computed(() => this.session.can('follow'));
  /** Matches the /wallet route's own canMatch guard (requirePermission('membership')) exactly —
   * showing this button to anyone without that permission (visitors, politicians, parties,
   * admins) sent them straight into a route-guard redirect back to /feed with no explanation. */
  protected readonly canJoinParty = computed(() => this.session.can('membership'));
  protected readonly isOwnParty = computed(() => !!this.party().id && this.session.account().id === this.party().id);

  protected readonly uploadingLogo = signal(false);
  protected readonly uploadingCover = signal(false);
  private readonly logoInput = viewChild<ElementRef<HTMLInputElement>>('logoInput');
  private readonly coverInput = viewChild<ElementRef<HTMLInputElement>>('coverInput');

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

  protected pickLogo(): void {
    this.logoInput()?.nativeElement.click();
  }

  protected pickCover(): void {
    this.coverInput()?.nativeElement.click();
  }

  protected onLogoSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) {
      return;
    }
    this.uploadingLogo.set(true);
    this.media.upload(file).subscribe({
      next: (url) => this.partyService.updateLogo(url).subscribe({
        complete: () => this.uploadingLogo.set(false),
        error: () => this.uploadingLogo.set(false),
      }),
      error: () => this.uploadingLogo.set(false),
    });
  }

  protected onCoverSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) {
      return;
    }
    this.uploadingCover.set(true);
    this.media.upload(file).subscribe({
      next: (url) => this.partyService.updateCoverPhoto(url).subscribe({
        complete: () => this.uploadingCover.set(false),
        error: () => this.uploadingCover.set(false),
      }),
      error: () => this.uploadingCover.set(false),
    });
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

  protected onVote(event: VoteEvent): void {
    this.feedService.vote(event.postId, event.optionId);
  }

  protected onUnvote(postId: string): void {
    this.feedService.unvote(postId);
  }

  protected readonly hasStatute = computed(() => !!this.party().statuteUrl && this.party().statuteUrl !== '#');

  protected readonly youtubeVideoId = computed(() => {
    const url = this.party().videoUrl;
    return url ? extractYouTubeId(url) : null;
  });

  protected openStatute(): void {
    const url = this.party().statuteUrl;
    if (url && url !== '#') {
      window.open(url, '_blank', 'noopener');
    }
  }

  /** Agenda = scheduled/upcoming; Events = past history. Same underlying list, split by date —
   * there's no separate "agenda" data source, just a different time-window view of party events. */
  protected readonly upcomingEvents = computed(() => {
    const now = Date.now();
    return this.party()
      .events.filter((e) => new Date(e.date).getTime() >= now)
      .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
  });

  protected readonly pastEvents = computed(() => {
    const now = Date.now();
    return this.party()
      .events.filter((e) => new Date(e.date).getTime() < now)
      .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  });

  protected readonly foundedYearDisplay = computed(() => {
    const year = this.party().foundedYear;
    return year !== null ? String(year) : '—';
  });

  protected readonly statItems = computed<StatStripItem[]>(() => {
    const p = this.party();
    const t = (key: string, fallback: string) => this.translate.t(key, fallback);
    return [
      {
        icon: 'groups',
        label: t('label.members', 'Members'),
        value: p.memberCount.toLocaleString('pt-BR'),
        caption: t('label.nationwide', 'Nationwide'),
      },
      {
        icon: 'location_city',
        label: t('label.directories', 'Directories'),
        value: p.directories.length.toString(),
        caption: t('label.active-levels', 'Active levels'),
      },
      {
        icon: 'event',
        label: t('label.founded', 'Founded'),
        value: this.foundedYearDisplay(),
        caption: t('label.years-of-history', 'Years of history'),
      },
      {
        icon: 'how_to_vote',
        label: t('label.representatives', 'Representatives'),
        value: p.representatives.length.toString(),
        caption: t('label.in-office', 'In office'),
      },
    ];
  });

  protected readonly overview = computed<DataListItem[]>(() => {
    const p = this.party();
    const t = (key: string, fallback: string) => this.translate.t(key, fallback);
    return [
      { icon: 'tag', label: t('label.number', 'Number'), value: String(p.number) },
      { icon: 'psychology', label: t('label.ideology', 'Ideology'), value: p.ideology },
      { icon: 'event', label: t('label.founded', 'Founded'), value: p.foundedYear !== null ? String(p.foundedYear) : '' },
      { icon: 'person', label: t('label.president', 'President'), value: p.president },
    ];
  });
}
