import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TextareaModule } from 'primeng/textarea';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { UiAvatar } from '../../../../shared/ui/ui-avatar/ui-avatar';
import { UiButton } from '../../../../shared/ui/ui-button/ui-button';
import { UiIconButton } from '../../../../shared/ui/ui-icon-button/ui-icon-button';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { UiTabs, UiTab } from '../../../../shared/ui/ui-tabs/ui-tabs';
import { PostDraft, PostKind, PostVisibility, UserSummary } from '../../../../core/models';

export type PostComposerMode = PostKind;

const MODES: UiTab[] = [
  { id: 'text', label: 'Text', icon: 'edit' },
  { id: 'agenda', label: 'Agenda', icon: 'event' },
  { id: 'live', label: 'Live', icon: 'sensors' },
];

/** Post composer. Presentational — emits `publish` with the drafted post. */
@Component({
  selector: 'app-post-composer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, TextareaModule, ToggleSwitchModule, UiAvatar, UiButton, UiIconButton, UiIcon, UiTabs],
  templateUrl: './post-composer.html',
  styleUrl: './post-composer.scss',
})
export class PostComposer {
  readonly author = input.required<UserSummary>();
  readonly publish = output<PostDraft>();

  protected readonly composerModes = MODES;
  protected readonly mode = signal<PostComposerMode>('text');

  protected readonly draft = signal('');
  /** true = public, false = private. */
  protected readonly isPublic = signal(true);

  protected readonly agendaTitle = signal('');
  protected readonly agendaDate = signal('');
  protected readonly agendaLocation = signal('');

  protected readonly liveVideoId = signal('');
  protected readonly liveChannelId = signal('');
  protected readonly liveIsNow = signal(true);
  protected readonly liveScheduledFor = signal('');

  protected readonly canPublish = computed(() => {
    switch (this.mode()) {
      case 'agenda':
        return (
          this.agendaTitle().trim().length > 0 &&
          this.agendaDate().trim().length > 0 &&
          this.agendaLocation().trim().length > 0
        );
      case 'live':
        return (
          (this.liveVideoId().trim().length > 0 || this.liveChannelId().trim().length > 0) &&
          (this.liveIsNow() || this.liveScheduledFor().trim().length > 0)
        );
      default:
        return this.draft().trim().length > 0;
    }
  });

  protected setMode(id: string): void {
    this.mode.set(id as PostComposerMode);
  }

  protected onPublish(): void {
    if (!this.canPublish()) {
      return;
    }
    const kind = this.mode();
    const visibility: PostVisibility = this.isPublic() ? 'public' : 'private';

    if (kind === 'agenda') {
      this.publish.emit({
        kind,
        text: this.draft().trim(),
        visibility,
        agenda: {
          title: this.agendaTitle().trim(),
          date: this.agendaDate().trim(),
          location: this.agendaLocation().trim(),
        },
      });
    } else if (kind === 'live') {
      this.publish.emit({
        kind,
        text: this.draft().trim(),
        visibility,
        live: {
          videoId: this.liveVideoId().trim(),
          channelId: this.liveChannelId().trim(),
          isLiveNow: this.liveIsNow(),
          scheduledFor: this.liveScheduledFor().trim(),
        },
      });
    } else {
      this.publish.emit({ kind, text: this.draft().trim(), visibility });
    }

    this.resetForm();
  }

  private resetForm(): void {
    this.draft.set('');
    this.isPublic.set(true);
    this.agendaTitle.set('');
    this.agendaDate.set('');
    this.agendaLocation.set('');
    this.liveVideoId.set('');
    this.liveChannelId.set('');
    this.liveIsNow.set(true);
    this.liveScheduledFor.set('');
    this.mode.set('text');
  }
}
