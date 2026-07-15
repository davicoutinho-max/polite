import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';
import { PoliticianService } from '../../core/services/politician.service';
import { FeedService } from '../../core/services/feed.service';
import { SessionService } from '../../core/services/session.service';
import { CommentEvent } from '../feed/components/post-card/post-card';
import { PostCard } from '../feed/components/post-card/post-card';
import { UiEmpty } from '../../shared/ui/ui-empty/ui-empty';
import { ProfileHeader } from './components/profile-header/profile-header';
import { ProfileTabs } from './components/profile-tabs/profile-tabs';
import { ProfileOverview } from './components/profile-overview/profile-overview';
import { ProfileParliamentary } from './components/profile-parliamentary/profile-parliamentary';
import { ProfileTransparency } from './components/profile-transparency/profile-transparency';
import { ProfileCareer } from './components/profile-career/profile-career';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Politician profile page — full dossier across tabbed sections. */
@Component({
  selector: 'app-profile',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ProfileHeader,
    ProfileTabs,
    PostCard,
    UiEmpty,
    ProfileOverview,
    ProfileParliamentary,
    ProfileTransparency,
    ProfileCareer,
    TranslatePipe,
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile {
  private readonly politicianService = inject(PoliticianService);
  private readonly feedService = inject(FeedService);
  private readonly session = inject(SessionService);
  private readonly route = inject(ActivatedRoute);

  protected readonly politician = this.politicianService.politician;
  protected readonly tabs = this.politicianService.tabs;
  protected readonly activity = this.politicianService.activity;
  protected readonly transparency = this.politicianService.transparency;
  protected readonly career = this.politicianService.career;

  protected readonly activityPosts = computed(() => this.feedService.postsByAuthor(this.politician().id)());
  protected readonly canReact = computed(() => this.session.can('react'));
  protected readonly currentUserAvatar = computed(() => this.session.currentUser().avatarUrl);

  protected readonly activeTab = signal('activity');

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const rawId = params.get('id');
      const id = !rawId || rawId === 'me' ? this.session.account().id : rawId;
      if (!id) {
        return;
      }
      forkJoin([
        this.politicianService.load(id),
        this.politicianService.loadActivity(id),
        this.politicianService.loadTransparency(id),
        this.politicianService.loadCareer(id),
      ]).subscribe();
    });
  }

  protected onTabChange(id: string): void {
    this.activeTab.set(id);
  }

  protected onLike(postId: string): void {
    this.feedService.toggleLike(postId);
  }

  protected onComment(event: CommentEvent): void {
    this.feedService.addComment(event.postId, event.text);
  }
}
