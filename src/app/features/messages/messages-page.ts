import { ChangeDetectionStrategy, Component, computed, ElementRef, inject, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { MessagesService } from '../../core/services/messages.service';
import { ChatMessage, Conversation, MessageAttachmentType } from '../../core/models';
import { InfiniteScrollDirective } from '../../core/directives/infinite-scroll.directive';
import { UiAvatar } from '../../shared/ui/ui-avatar/ui-avatar';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiIconButton } from '../../shared/ui/ui-icon-button/ui-icon-button';
import { UiEmpty } from '../../shared/ui/ui-empty/ui-empty';
import { CanDirective } from '../../core/directives/can.directive';
import { NewConversationPicker, NewConversationEvent } from './components/new-conversation-picker/new-conversation-picker';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { TranslateService } from '../../core/services/translate.service';

/** Generic group icon (two overlapping silhouettes) — shown for any group conversation that
 * hasn't had a real photo set yet, instead of defaulting to one member's personal avatar (which
 * misleadingly implied that member "was" the group). */
const GROUP_AVATAR_PLACEHOLDER =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 40 40'%3E%3Crect width='40' height='40' fill='%23c7ccd1'/%3E%3Ccircle cx='16' cy='15' r='6' fill='%23fff'/%3E%3Cpath d='M4 33c0-6.6 5.4-12 12-12s12 5.4 12 12' fill='%23fff'/%3E%3Ccircle cx='27' cy='14' r='5' fill='%23fff' opacity='0.6'/%3E%3Cpath d='M20 32c.6-5.9 5-10.5 10.8-10.5S40.8 26 41 32' fill='%23fff' opacity='0.6'/%3E%3C/svg%3E";

/** A small curated set rather than a full emoji library/picker dependency — covers the common
 * reactions/expressions people actually reach for in chat. */
const EMOJIS: readonly string[] = [
  '😀', '😂', '😅', '😊', '🙂', '😉', '😍', '😘', '😜', '🤔',
  '😎', '😢', '😭', '😡', '😱', '🥳', '😴', '🤯', '🙄', '😇',
  '👍', '👎', '👏', '🙏', '💪', '🤝', '✌️', '👌', '🤞', '👋',
  '❤️', '🔥', '🎉', '✅', '⭐', '💡', '📌', '⚡', '🗳️', '🏛️',
];

/** Direct messages: conversation list + thread with a composer. */
@Component({
  selector: 'app-messages-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    InputText,
    InfiniteScrollDirective,
    UiAvatar,
    UiIcon,
    UiIconButton,
    UiEmpty,
    CanDirective,
    NewConversationPicker,
    TranslatePipe,
  ],
  templateUrl: './messages-page.html',
  styleUrl: './messages-page.scss',
})
export class MessagesPage {
  private readonly messages = inject(MessagesService);
  private readonly translate = inject(TranslateService);

  protected readonly conversations = this.messages.conversations;
  protected readonly activeId = this.messages.activeId;
  protected readonly active = this.messages.active;

  /** Mobile: whether the thread pane is shown over the list. */
  protected readonly showThread = signal(false);
  protected readonly draft = signal('');
  protected readonly showPicker = signal(false);

  // ---- Emoji picker ----
  protected readonly emojis = EMOJIS;
  protected readonly showEmojiPicker = signal(false);

  // ---- Group settings (3-dot menu: rename, change photo) ----
  protected readonly showGroupMenu = signal(false);
  protected readonly renamingGroup = signal(false);
  protected readonly groupNameDraft = signal('');

  // ---- Message edit/delete ----
  protected readonly editingMessageId = signal<string | null>(null);
  protected readonly editDraft = signal('');

  // ---- Reply-to-message ----
  protected readonly replyingTo = signal<ChatMessage | null>(null);

  // ---- Real-time: typing indicator ----
  protected readonly typingByConversation = this.messages.typingByConversation;
  private lastTypingEmitAt = 0;

  // ---- Real-time: recording indicator + voice messages ----
  protected readonly recordingByConversation = this.messages.recordingByConversation;
  protected readonly isRecording = signal(false);
  /** Local feedback for the person actually recording — a running mm:ss counter next to the
   * mic button, distinct from `recordingByConversation` (which is what the OTHER participant
   * sees for the same event). */
  protected readonly recordingSeconds = signal(0);
  protected readonly recordingTimeLabel = computed(() => {
    const total = this.recordingSeconds();
    const minutes = Math.floor(total / 60);
    const seconds = total % 60;
    return `${minutes}:${String(seconds).padStart(2, '0')}`;
  });
  private mediaRecorder: MediaRecorder | null = null;
  private recordedChunks: Blob[] = [];
  private discardCurrentRecording = false;
  private recordingPingInterval: ReturnType<typeof setInterval> | null = null;
  private recordingTimerInterval: ReturnType<typeof setInterval> | null = null;

  // ---- History infinite scroll (load older messages on scroll-to-top) ----
  private readonly threadBody = viewChild<ElementRef<HTMLElement>>('threadBody');

  protected onSelect(id: string): void {
    this.messages.select(id);
    this.showThread.set(true);
    this.closeGroupMenu();
    this.cancelEditMessage();
  }

  protected onBack(): void {
    this.showThread.set(false);
  }

  protected onSend(): void {
    if (!this.draft().trim()) {
      return;
    }
    this.messages.send(this.draft(), this.replyingTo()?.id);
    this.draft.set('');
    this.replyingTo.set(null);
  }

  // ---- Emoji picker ----

  protected toggleEmojiPicker(): void {
    this.showEmojiPicker.update((v) => !v);
  }

  protected closeEmojiPicker(): void {
    this.showEmojiPicker.set(false);
  }

  protected insertEmoji(emoji: string): void {
    this.draft.update((text) => text + emoji);
    this.showEmojiPicker.set(false);
  }

  protected openPicker(): void {
    this.showPicker.set(true);
  }

  protected closePicker(): void {
    this.showPicker.set(false);
  }

  protected onCreateConversation(event: NewConversationEvent): void {
    this.messages.createConversation(event.participants, event.groupName).subscribe({
      next: () => {
        this.showPicker.set(false);
        this.showThread.set(true);
      },
    });
  }

  /** Display name: the single peer's name for 1:1, or the group name for groups. */
  protected peerName(c: Conversation): string {
    return c.isGroup ? c.groupName ?? this.translate.t('label.group', 'Group') : c.participants[0]?.name ?? '';
  }

  protected peerAvatar(c: Conversation): string {
    return c.isGroup ? c.groupAvatarUrl || GROUP_AVATAR_PLACEHOLDER : c.participants[0]?.avatarUrl ?? '';
  }

  protected peerRole(c: Conversation): string {
    return c.isGroup
      ? `${c.participants.length} ${this.translate.t('label.participants', 'participants')}`
      : c.participants[0]?.role ?? '';
  }

  // ---- Group settings ----

  protected toggleGroupMenu(): void {
    this.showGroupMenu.update((v) => !v);
  }

  protected closeGroupMenu(): void {
    this.showGroupMenu.set(false);
  }

  protected startRenameGroup(currentName: string): void {
    this.groupNameDraft.set(currentName);
    this.renamingGroup.set(true);
    this.showGroupMenu.set(false);
  }

  protected cancelRenameGroup(): void {
    this.renamingGroup.set(false);
  }

  protected saveRenameGroup(conversationId: string): void {
    const name = this.groupNameDraft().trim();
    if (name) {
      this.messages.renameGroup(conversationId, name);
    }
    this.renamingGroup.set(false);
  }

  protected onGroupAvatarSelected(conversationId: string, event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    this.showGroupMenu.set(false);
    if (file) {
      this.messages.changeGroupAvatarFile(conversationId, file);
    }
  }

  // ---- Message edit/delete ----

  protected startEditMessage(message: ChatMessage): void {
    this.editingMessageId.set(message.id);
    this.editDraft.set(message.text);
  }

  protected cancelEditMessage(): void {
    this.editingMessageId.set(null);
    this.editDraft.set('');
  }

  protected saveEditMessage(conversationId: string): void {
    const messageId = this.editingMessageId();
    const text = this.editDraft().trim();
    if (messageId && text) {
      this.messages.editMessage(conversationId, messageId, text);
    }
    this.cancelEditMessage();
  }

  protected deleteMessage(conversationId: string, messageId: string): void {
    this.messages.deleteMessage(conversationId, messageId);
  }

  // ---- Reply-to-message ----

  /** Any message — not just my own — can be replied to, WhatsApp-style. */
  protected startReply(message: ChatMessage): void {
    this.cancelEditMessage();
    this.replyingTo.set(message);
  }

  protected cancelReply(): void {
    this.replyingTo.set(null);
  }

  /** Resolves the quoted message against the conversation's already-loaded list — the reply
   * relationship is a soft, display-only reference (see the backend's own reasoning), so an
   * older message that's since scrolled out of the loaded window just falls back to a generic
   * placeholder rather than firing a dedicated fetch. */
  protected replyPreview(conversation: Conversation, message: ChatMessage): ChatMessage | null {
    if (!message.replyToMessageId) {
      return null;
    }
    return conversation.messages.find((m) => m.id === message.replyToMessageId) ?? null;
  }

  protected replyPreviewLabel(preview: ChatMessage | null): string {
    if (!preview) {
      return this.translate.t('label.original-message', 'Original message');
    }
    if (preview.deleted) {
      return this.translate.t('label.message-deleted', 'This message was deleted');
    }
    if (preview.attachmentType) {
      return `📎 ${preview.attachmentFileName || preview.attachmentType}`;
    }
    return preview.text;
  }

  // ---- Real-time: typing indicator + seen receipt ----

  /** Throttled so every keystroke doesn't fan out a ping — one "still typing" ping per 2s max. */
  protected onComposerInput(conversationId: string): void {
    const now = Date.now();
    if (now - this.lastTypingEmitAt > 2000) {
      this.lastTypingEmitAt = now;
      this.messages.sendTyping(conversationId);
    }
  }

  protected isPeerTyping(conversation: Conversation): boolean {
    return (this.typingByConversation()[conversation.id]?.size ?? 0) > 0;
  }

  protected isPeerRecording(conversation: Conversation): boolean {
    return (this.recordingByConversation()[conversation.id]?.size ?? 0) > 0;
  }

  // ---- Attachments: file/image/video + voice messages ----

  /** Generic attach button — the browser file picker itself lets the user filter by type; the
   * attachment's kind is inferred from the picked file's MIME type rather than requiring a
   * separate control per media kind. */
  protected onAttachmentSelected(conversationId: string, event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    (event.target as HTMLInputElement).value = '';
    if (!file) {
      return;
    }
    const type: MessageAttachmentType = file.type.startsWith('image/')
      ? 'image'
      : file.type.startsWith('video/')
        ? 'video'
        : file.type.startsWith('audio/')
          ? 'audio'
          : 'file';
    this.messages.sendAttachment(conversationId, file, type, '', this.replyingTo()?.id);
    this.replyingTo.set(null);
  }

  protected async startRecording(conversationId: string): Promise<void> {
    if (this.isRecording()) {
      return;
    }
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.recordedChunks = [];
      this.discardCurrentRecording = false;
      const recorder = new MediaRecorder(stream);
      const replyToMessageId = this.replyingTo()?.id;
      recorder.ondataavailable = (e) => {
        if (e.data.size > 0) {
          this.recordedChunks.push(e.data);
        }
      };
      recorder.onstop = () => {
        stream.getTracks().forEach((track) => track.stop());
        if (this.discardCurrentRecording) {
          return;
        }
        const blob = new Blob(this.recordedChunks, { type: recorder.mimeType || 'audio/webm' });
        if (blob.size > 0) {
          const file = new File([blob], `voice-message.${blob.type.includes('mp4') ? 'm4a' : 'webm'}`, { type: blob.type });
          this.messages.sendAttachment(conversationId, file, 'audio', '', replyToMessageId);
        }
      };
      this.mediaRecorder = recorder;
      recorder.start();
      this.isRecording.set(true);
      this.replyingTo.set(null);
      this.recordingSeconds.set(0);
      this.recordingTimerInterval = setInterval(() => this.recordingSeconds.update((s) => s + 1), 1000);
      this.messages.sendRecording(conversationId);
      this.recordingPingInterval = setInterval(() => this.messages.sendRecording(conversationId), 2000);
    } catch {
      this.isRecording.set(false);
    }
  }

  /** The checkmark/send button — stops recording and uploads+sends the voice message. */
  protected stopRecordingAndSend(): void {
    this.discardCurrentRecording = false;
    this.teardownRecording();
  }

  /** The trash button — stops recording and throws the audio away; no message is ever sent. */
  protected cancelRecording(): void {
    this.discardCurrentRecording = true;
    this.teardownRecording();
  }

  private teardownRecording(): void {
    this.mediaRecorder?.stop();
    this.mediaRecorder = null;
    this.isRecording.set(false);
    this.recordingSeconds.set(0);
    if (this.recordingPingInterval !== null) {
      clearInterval(this.recordingPingInterval);
      this.recordingPingInterval = null;
    }
    if (this.recordingTimerInterval !== null) {
      clearInterval(this.recordingTimerInterval);
      this.recordingTimerInterval = null;
    }
  }

  /** WhatsApp-style: only the very last message I sent shows a "Seen" tag, once a peer's read
   * receipt timestamp catches up to when it was sent. */
  protected isSeen(conversation: Conversation, message: ChatMessage): boolean {
    if (!message.fromMe || message.deleted) {
      return false;
    }
    if (conversation.messages.at(-1)?.id !== message.id) {
      return false;
    }
    return !!conversation.peerReadAt && conversation.peerReadAt >= message.createdAt;
  }

  // ---- History infinite scroll ----

  /** Prepending older messages above the scroll viewport would otherwise yank the view down to
   * the new top — this pins the visual scroll position to whatever the user was looking at by
   * measuring the height added and offsetting scrollTop by exactly that much once Angular has
   * rendered the prepended messages. */
  protected loadOlderMessages(conversationId: string): void {
    const el = this.threadBody()?.nativeElement;
    const previousHeight = el?.scrollHeight ?? 0;
    this.messages.loadOlderMessages(conversationId);
    if (!el) {
      return;
    }
    requestAnimationFrame(() => {
      el.scrollTop += el.scrollHeight - previousHeight;
    });
  }
}
