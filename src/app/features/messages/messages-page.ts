import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { MessagesService } from '../../core/services/messages.service';
import { ChatMessage, Conversation } from '../../core/models';
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

/** Direct messages: conversation list + thread with a composer. */
@Component({
  selector: 'app-messages-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, InputText, UiAvatar, UiIcon, UiIconButton, UiEmpty, CanDirective, NewConversationPicker, TranslatePipe],
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

  // ---- Group settings (3-dot menu: rename, change photo) ----
  protected readonly showGroupMenu = signal(false);
  protected readonly renamingGroup = signal(false);
  protected readonly groupNameDraft = signal('');

  // ---- Message edit/delete ----
  protected readonly editingMessageId = signal<string | null>(null);
  protected readonly editDraft = signal('');

  // ---- Real-time: typing indicator ----
  protected readonly typingByConversation = this.messages.typingByConversation;
  private lastTypingEmitAt = 0;

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
    this.messages.send(this.draft());
    this.draft.set('');
  }

  protected openPicker(): void {
    this.showPicker.set(true);
  }

  protected closePicker(): void {
    this.showPicker.set(false);
  }

  protected onCreateConversation(event: NewConversationEvent): void {
    this.messages.createConversation(event.participants, event.groupName);
    this.showPicker.set(false);
    this.showThread.set(true);
  }

  /** Display name: the single peer's name for 1:1, or the group name for groups. */
  protected peerName(c: Conversation): string {
    return c.isGroup ? c.groupName ?? this.translate.t('label.group', 'Group') : c.participants[0]?.name ?? '';
  }

  protected peerAvatar(c: Conversation): string {
    return c.isGroup ? c.groupAvatarUrl || GROUP_AVATAR_PLACEHOLDER : c.participants[0]?.avatarUrl ?? '';
  }

  protected peerVerified(c: Conversation): boolean {
    return !c.isGroup && !!c.participants[0]?.verified;
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
}
