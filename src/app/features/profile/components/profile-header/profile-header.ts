import { ChangeDetectionStrategy, Component, ElementRef, inject, input, output, signal, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Politician } from '../../../../core/models';
import { MediaService } from '../../../../core/services/media.service';
import { PoliticianService } from '../../../../core/services/politician.service';
import { UiButton } from '../../../../shared/ui/ui-button/ui-button';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

/** Politician profile hero: cover, avatar, identity and primary actions. Mostly presentational —
 * follow state and the follow/contact actions themselves live in the parent (Profile), backed by
 * DirectoryService/MessagesService — but avatar/cover upload is self-contained here since it's a
 * purely local interaction that only ever applies to the signed-in politician's own profile. */
@Component({
  selector: 'app-profile-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiButton, UiIcon, RouterLink, TranslatePipe],
  templateUrl: './profile-header.html',
  styleUrl: './profile-header.scss',
})
export class ProfileHeader {
  private readonly media = inject(MediaService);
  private readonly politicianService = inject(PoliticianService);

  readonly politician = input.required<Politician>();
  readonly following = input(false);
  readonly canFollow = input(true);
  readonly canContact = input(true);
  /** Only true for citizens viewing a politician that has a party — politicians/parties/admin
   * accounts don't hold the 'request-affiliation' permission themselves (see permission.model.ts). */
  readonly canAffiliate = input(false);
  /** True only when the signed-in account is this exact politician — shows the avatar/cover
   * upload controls instead of follow/contact. */
  readonly isOwnProfile = input(false);

  readonly toggleFollow = output<void>();
  readonly contact = output<void>();

  protected readonly uploadingAvatar = signal(false);
  protected readonly uploadingCover = signal(false);

  private readonly avatarInput = viewChild<ElementRef<HTMLInputElement>>('avatarInput');
  private readonly coverInput = viewChild<ElementRef<HTMLInputElement>>('coverInput');

  protected pickAvatar(): void {
    this.avatarInput()?.nativeElement.click();
  }

  protected pickCover(): void {
    this.coverInput()?.nativeElement.click();
  }

  protected onAvatarSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) {
      return;
    }
    this.uploadingAvatar.set(true);
    this.media.upload(file).subscribe({
      next: (url) => this.politicianService.updateProfileImages(url, undefined).subscribe({
        complete: () => this.uploadingAvatar.set(false),
        error: () => this.uploadingAvatar.set(false),
      }),
      error: () => this.uploadingAvatar.set(false),
    });
  }

  protected onCoverSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) {
      return;
    }
    this.uploadingCover.set(true);
    this.media.upload(file).subscribe({
      next: (url) => this.politicianService.updateProfileImages(undefined, url).subscribe({
        complete: () => this.uploadingCover.set(false),
        error: () => this.uploadingCover.set(false),
      }),
      error: () => this.uploadingCover.set(false),
    });
  }
}
