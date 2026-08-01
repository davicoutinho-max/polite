import { ChangeDetectionStrategy, Component, computed, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TextareaModule } from 'primeng/textarea';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { FileSelectEvent, FileUpload } from 'primeng/fileupload';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { DatePicker } from 'primeng/datepicker';
import { UiAvatar } from '../../../../shared/ui/ui-avatar/ui-avatar';
import { UiButton } from '../../../../shared/ui/ui-button/ui-button';
import { UiIconButton } from '../../../../shared/ui/ui-icon-button/ui-icon-button';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { UiTabs, UiTab } from '../../../../shared/ui/ui-tabs/ui-tabs';
import { UiDialog } from '../../../../shared/ui/ui-dialog/ui-dialog';
import { PlatformService } from '../../../../core/services/platform.service';
import { TranslateService } from '../../../../core/services/translate.service';
import { AiAssistantService, SocialVariants } from '../../../../core/services/ai-assistant.service';
import { AlertsService } from '../../../../core/services/alerts.service';
import { SocialConnection, SocialConnectionService, SocialPlatform } from '../../../../core/services/social-connection.service';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { PostDraft, PostKind, PostVisibility, UserSummary } from '../../../../core/models';

const VARIANT_TABS: UiTab[] = [
  { id: 'instagram', label: 'Instagram', key: 'tab.variant-instagram', icon: 'photo_camera' },
  { id: 'facebook', label: 'Facebook', key: 'tab.variant-facebook', icon: 'thumb_up' },
  { id: 'x', label: 'X', key: 'tab.variant-x', icon: 'flutter_dash' },
  { id: 'linkedin', label: 'LinkedIn', key: 'tab.variant-linkedin', icon: 'work' },
  { id: 'simpleSummary', label: 'Simple summary', key: 'tab.variant-simple-summary', icon: 'chat_bubble' },
];

export type PostComposerMode = PostKind;

const MODES: UiTab[] = [
  { id: 'text', label: 'Text', key: 'tab.post-text', icon: 'edit' },
  { id: 'agenda', label: 'Agenda', key: 'tab.post-agenda', icon: 'event' },
  { id: 'live', label: 'Live', key: 'tab.post-live', icon: 'sensors' },
];

/** Post composer. Presentational — emits `publish` with the drafted post. */
@Component({
  selector: 'app-post-composer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    TextareaModule,
    ToggleSwitchModule,
    FileUpload,
    InputText,
    Select,
    DatePicker,
    UiAvatar,
    UiButton,
    UiIconButton,
    UiIcon,
    UiTabs,
    UiDialog,
    TranslatePipe,
  ],
  templateUrl: './post-composer.html',
  styleUrl: './post-composer.scss',
})
export class PostComposer {
  private readonly platform = inject(PlatformService);
  protected readonly translate = inject(TranslateService);
  private readonly aiAssistant = inject(AiAssistantService);
  private readonly alerts = inject(AlertsService);
  private readonly socialConnectionService = inject(SocialConnectionService);

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
  /** 'none' = the poll never locks on its own — voting/unvoting/switching stays open forever. */
  protected readonly pollDuration = signal<'none' | '1d' | '3d' | '1w'>('1d');
  protected readonly pollDurationOptions: { value: 'none' | '1d' | '3d' | '1w'; label: string }[] = [
    { value: 'none', label: this.translate.t('label.poll-no-time-limit', 'No time limit') },
    { value: '1d', label: this.translate.t('label.poll-1-day', '1 day') },
    { value: '3d', label: this.translate.t('label.poll-3-days', '3 days') },
    { value: '1w', label: this.translate.t('label.poll-1-week', '1 week') },
  ];

  private readonly pollOptionsValid = computed(
    () => this.pollOptionInputs().filter((o) => o.trim().length > 0).length >= 2,
  );

  protected readonly canPublish = computed(() => {
    if (this.pollMode() && (!this.pollOptionsValid() || this.draft().trim().length === 0)) {
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

  // ---- Cross-posting to connected real social networks (Facebook/Instagram/X) ----
  protected readonly socialConnections = signal<SocialConnection[]>([]);
  protected readonly selectedSocialPlatforms = signal<Set<SocialPlatform>>(new Set());
  protected readonly hasSocialConnections = computed(() => this.socialConnections().length > 0);

  constructor() {
    this.socialConnectionService.listConnections().subscribe({
      next: (list) => this.socialConnections.set(list),
      error: () => undefined,
    });
  }

  protected isSocialPlatformSelected(platform: SocialPlatform): boolean {
    return this.selectedSocialPlatforms().has(platform);
  }

  protected toggleSocialPlatform(platform: SocialPlatform): void {
    this.selectedSocialPlatforms.update((selected) => {
      const next = new Set(selected);
      if (next.has(platform)) {
        next.delete(platform);
      } else {
        next.add(platform);
      }
      return next;
    });
  }

  protected setMode(id: string): void {
    this.mode.set(id as PostComposerMode);
  }

  /** The draft textarea's placeholder depends on both poll mode and post kind — kept here rather
   * than inline in the template so it can call TranslateService without an awkward nested-ternary
   * translate-pipe chain. */
  protected draftPlaceholder(): string {
    if (this.pollMode()) {
      return this.translate.t('label.poll-title-placeholder', 'Poll title — ask your question…');
    }
    if (this.mode() === 'text') {
      return this.translate.t('label.policy-discussion-placeholder', 'Start a policy discussion...');
    }
    return this.translate.t('label.optional-note-placeholder', 'Add a note (optional)...');
  }

  protected optionPlaceholder(index: number): string {
    return this.translate.t('label.option-n', 'Option {n}').replace('{n}', String(index + 1));
  }

  protected onImageSelected(event: FileSelectEvent): void {
    const file = event.files[0];
    if (file) {
      this.imageFile.set(file);
    }
  }

  protected removeImage(): void {
    this.imageFile.set(null);
  }

  protected onFileSelected(event: FileSelectEvent): void {
    const file = event.files[0];
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

  private computePollClosesAt(): string | undefined {
    const duration = this.pollDuration();
    if (duration === 'none') {
      return undefined;
    }
    const hours = { '1d': 24, '3d': 72, '1w': 168 }[duration];
    return new Date(Date.now() + hours * 3600_000).toISOString();
  }

  // ---- AI-generated social-media variants (Instagram/Facebook/X/LinkedIn + plain summary) ----
  // A preview of what could be cross-posted once real network connections exist (see item 2 of
  // this feature) — for now, a politician reviews/edits and copies each version by hand.
  protected readonly variantTabs = VARIANT_TABS;
  protected readonly showVariantsDialog = signal(false);
  protected readonly variantsLoading = signal(false);
  protected readonly variantsError = signal(false);
  protected readonly variants = signal<SocialVariants | null>(null);
  protected readonly activeVariantTab = signal('instagram');

  protected readonly canGenerateVariants = computed(() => this.draft().trim().length > 0);

  protected openVariantsDialog(): void {
    const text = this.draft().trim();
    if (!text) {
      return;
    }
    this.showVariantsDialog.set(true);
    this.variantsError.set(false);
    this.variantsLoading.set(true);
    this.activeVariantTab.set('instagram');
    this.aiAssistant.generateSocialVariants(text).subscribe({
      next: (result) => {
        this.variantsLoading.set(false);
        this.variants.set(result);
      },
      error: () => {
        this.variantsLoading.set(false);
        this.variantsError.set(true);
      },
    });
  }

  protected closeVariantsDialog(): void {
    this.showVariantsDialog.set(false);
  }

  protected activeVariantText(): string {
    const variants = this.variants();
    if (!variants) {
      return '';
    }
    return variants[this.activeVariantTab() as keyof SocialVariants];
  }

  protected copyActiveVariant(): void {
    const text = this.activeVariantText();
    if (!text) {
      return;
    }
    navigator.clipboard
      .writeText(text)
      .then(() =>
        this.alerts.push({
          category: 'project',
          icon: 'content_copy',
          title: this.translate.t('title.copied-to-clipboard', 'Copied to clipboard'),
          message: this.translate.t('hint.copied-to-clipboard', 'Paste it directly into the app of your choice.'),
          timeLabel: this.translate.t('label.just-now', 'Just now'),
        }),
      )
      .catch(() => undefined);
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
      pollClosesAt: this.pollMode() ? this.computePollClosesAt() : undefined,
      socialPlatforms: this.selectedSocialPlatforms().size > 0 ? Array.from(this.selectedSocialPlatforms()) : undefined,
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
    this.pollDuration.set('1d');
    this.mode.set('text');
    this.selectedSocialPlatforms.set(new Set());
  }
}
