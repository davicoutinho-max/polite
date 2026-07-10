import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MessagesService } from '../../core/services/messages.service';
import { Conversation } from '../../core/models';
import { UiAvatar } from '../../shared/ui/ui-avatar/ui-avatar';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiIconButton } from '../../shared/ui/ui-icon-button/ui-icon-button';
import { CanDirective } from '../../core/directives/can.directive';
import { NewConversationPicker, NewConversationEvent } from './components/new-conversation-picker/new-conversation-picker';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { TranslateService } from '../../core/services/translate.service';

/** Direct messages: conversation list + thread with a composer. */
@Component({
  selector: 'app-messages-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, UiAvatar, UiIcon, UiIconButton, CanDirective, NewConversationPicker, TranslatePipe],
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

  protected onSelect(id: string): void {
    this.messages.select(id);
    this.showThread.set(true);
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
    return c.isGroup ? c.groupAvatarUrl ?? c.participants[0]?.avatarUrl ?? '' : c.participants[0]?.avatarUrl ?? '';
  }

  protected peerVerified(c: Conversation): boolean {
    return !c.isGroup && !!c.participants[0]?.verified;
  }

  protected peerRole(c: Conversation): string {
    return c.isGroup
      ? `${c.participants.length} ${this.translate.t('label.participants', 'participants')}`
      : c.participants[0]?.role ?? '';
  }
}
