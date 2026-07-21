import { ChangeDetectionStrategy, Component, computed, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TextareaModule } from 'primeng/textarea';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { DatePicker } from 'primeng/datepicker';
import { UiAvatar } from '../../../../shared/ui/ui-avatar/ui-avatar';
import { UiButton } from '../../../../shared/ui/ui-button/ui-button';
import { UiIconButton } from '../../../../shared/ui/ui-icon-button/ui-icon-button';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { UiTabs, UiTab } from '../../../../shared/ui/ui-tabs/ui-tabs';
import { PlatformService } from '../../../../core/services/platform.service';
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
  imports: [
    FormsModule,
    TextareaModule,
    ToggleSwitchModule,
    InputText,
    Select,
    DatePicker,
    UiAvatar,
    UiButton,
    UiIconButton,
    UiIcon,
    UiTabs,
  ],
  templateUrl: './post-composer.html',
  styleUrl: './post-composer.scss',
})
export class PostComposer {
  private readonly platform = inject(PlatformService);

  readonly author = input.required<UserSummary>();
  readonly publish = output<PostDraft>();

  protected readonly composerModes = MODES;
  protected readonly mode = signal<PostComposerMode>('text');

  protected readonly draft = signal('');
  /** true = public, false = private. */
  protected readonly isPublic = signal(true);
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly agendaTitle = signal('');
  protected readonly agendaDate = signal<Date | null>(null);
  protected readonly agendaTime = signal<Date | null>(null);
  protected readonly agendaLocation = signal('');
  /** States registered in the platform admin's geography parameters — the agenda's location is
   * picked from this list rather than typed freely, so agenda locations stay consistent. */
  protected readonly states = this.platform.states;

  protected readonly liveVideoId = signal('');
  protected readonly liveChannelId = signal('');
  protected readonly liveIsNow = signal(true);
  protected readonly liveScheduledFor = signal('');

  /** Attachments — available regardless of mode (text/agenda/live all support them). */
  protected readonly imageFile = signal<File | null>(null);
  protected readonly imagePreviewUrl = computed(() => {
    const file = this.imageFile();
    return file ? URL.createObjectURL(file) : null;
  });
  protected readonly attachedFile = signal<File | null>(null);
  protected readonly pollMode = signal(false);
  protected readonly pollOptionInputs = signal<string[]>(['', '']);

  private readonly pollOptionsValid = computed(
    () => this.pollOptionInputs().filter((o) => o.trim().length > 0).length >= 2,
  );

  protected readonly canPublish = computed(() => {
    if (this.pollMode() && !this.pollOptionsValid()) {
      return false;
    }
    switch (this.mode()) {
      case 'agenda':
        return (
          this.agendaTitle().trim().length > 0 &&
          this.agendaDate() !== null &&
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

  protected onImageSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      this.imageFile.set(file);
    }
  }

  protected removeImage(): void {
    this.imageFile.set(null);
  }

  protected onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      this.attachedFile.set(file);
    }
  }

  protected removeAttachedFile(): void {
    this.attachedFile.set(null);
  }

  protected togglePoll(): void {
    this.pollMode.update((on) => !on);
    if (!this.pollMode()) {
      this.pollOptionInputs.set(['', '']);
    }
  }

  protected updatePollOption(index: number, value: string): void {
    this.pollOptionInputs.update((options) => options.map((o, i) => (i === index ? value : o)));
  }

  protected addPollOption(): void {
    if (this.pollOptionInputs().length < 6) {
      this.pollOptionInputs.update((options) => [...options, '']);
    }
  }

  protected removePollOption(index: number): void {
    if (this.pollOptionInputs().length > 2) {
      this.pollOptionInputs.update((options) => options.filter((_, i) => i !== index));
    }
  }

  protected onPublish(): void {
    if (!this.canPublish() || this.submitting()) {
      return;
    }
    const kind = this.mode();
    const visibility: PostVisibility = this.isPublic() ? 'public' : 'private';
    const attachments = {
      imageFile: this.imageFile() ?? undefined,
      attachedFile: this.attachedFile() ?? undefined,
      pollOptions: this.pollMode() ? this.pollOptionInputs().map((o) => o.trim()).filter((o) => o.length > 0) : undefined,
    };

    if (kind === 'agenda') {
      const date = this.agendaDate()!;
      const isoDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
      const time = this.agendaTime();
      const isoTime = time ? `${String(time.getHours()).padStart(2, '0')}:${String(time.getMinutes()).padStart(2, '0')}` : '';
      this.publish.emit({
        kind,
        text: this.draft().trim(),
        visibility,
        agenda: {
          title: this.agendaTitle().trim(),
          date: isoTime ? `${isoDate}T${isoTime}` : isoDate,
          location: this.agendaLocation().trim(),
        },
        ...attachments,
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
        ...attachments,
      });
    } else {
      this.publish.emit({ kind, text: this.draft().trim(), visibility, ...attachments });
    }
  }

  /** Called by Feed once the publish HTTP call actually succeeds — see the `submitting()` guard
   * above, which stops the user from firing a second publish while the first is still pending. */
  markSubmitting(): void {
    this.submitting.set(true);
    this.error.set(null);
  }

  onPublishSucceeded(): void {
    this.submitting.set(false);
    this.resetForm();
  }

  /** The draft is deliberately left in place on failure — clearing it here, like the old
   * unconditional reset used to, would silently discard what the user wrote the moment their post
   * failed to save (e.g. a lapsed session or a network error). */
  onPublishFailed(message: string): void {
    this.submitting.set(false);
    this.error.set(message);
  }

  private resetForm(): void {
    this.draft.set('');
    this.isPublic.set(true);
    this.agendaTitle.set('');
    this.agendaDate.set(null);
    this.agendaTime.set(null);
    this.agendaLocation.set('');
    this.liveVideoId.set('');
    this.liveChannelId.set('');
    this.liveIsNow.set(true);
    this.liveScheduledFor.set('');
    this.imageFile.set(null);
    this.attachedFile.set(null);
    this.pollMode.set(false);
    this.pollOptionInputs.set(['', '']);
    this.mode.set('text');
  }
}
