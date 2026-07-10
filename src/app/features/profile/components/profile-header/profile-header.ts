import { ChangeDetectionStrategy, Component, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Politician } from '../../../../core/models';
import { UiButton } from '../../../../shared/ui/ui-button/ui-button';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

/** Politician profile hero: cover, avatar, identity and primary actions. */
@Component({
  selector: 'app-profile-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiButton, UiIcon, RouterLink, TranslatePipe],
  templateUrl: './profile-header.html',
  styleUrl: './profile-header.scss',
})
export class ProfileHeader {
  readonly politician = input.required<Politician>();

  protected readonly following = signal(false);

  protected toggleFollow(): void {
    this.following.update((v) => !v);
  }
}
