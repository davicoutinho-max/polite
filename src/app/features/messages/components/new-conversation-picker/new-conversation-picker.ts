import { ChangeDetectionStrategy, Component, computed, inject, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { DirectoryService } from '../../../../core/services/directory.service';
import { UserSummary } from '../../../../core/models';
import { UiAvatar } from '../../../../shared/ui/ui-avatar/ui-avatar';
import { UiButton } from '../../../../shared/ui/ui-button/ui-button';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

export interface NewConversationEvent {
  readonly participants: UserSummary[];
  readonly groupName?: string;
}

/**
 * Picker for starting a new conversation. Candidates are always politicians
 * and parties — never citizens — regardless of who opens the picker (any
 * authenticated account with the `message` permission may open it); that
 * restriction lives here as the data source, not as a permission gate on the
 * entry-point button.
 */
@Component({
  selector: 'app-new-conversation-picker',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, InputText, IconField, InputIcon, UiAvatar, UiButton, UiIcon, TranslatePipe],
  templateUrl: './new-conversation-picker.html',
  styleUrl: './new-conversation-picker.scss',
})
export class NewConversationPicker {
  private readonly directory = inject(DirectoryService);

  readonly create = output<NewConversationEvent>();
  readonly close = output<void>();

  protected readonly search = signal('');
  protected readonly selectedIds = signal<ReadonlySet<string>>(new Set());
  protected readonly groupName = signal('');

  private readonly candidates = computed<UserSummary[]>(() => {
    const politicians: UserSummary[] = this.directory.politicians().map((p) => ({
      id: p.id,
      name: p.name,
      handle: p.handle,
      avatarUrl: p.avatarUrl,
      verified: p.verified,
      role: [p.office, p.partyAcronym].filter(Boolean).join(' · '),
    }));
    const parties: UserSummary[] = this.directory.parties().map((p) => ({
      id: p.id,
      name: p.name,
      handle: p.acronym,
      avatarUrl: p.logoUrl,
      verified: true,
      role: 'Party',
    }));
    return [...politicians, ...parties];
  });

  protected readonly filtered = computed<UserSummary[]>(() => {
    const q = this.search().trim().toLowerCase();
    if (!q) return this.candidates();
    return this.candidates().filter(
      (c) => c.name.toLowerCase().includes(q) || (c.handle ?? '').toLowerCase().includes(q),
    );
  });

  protected readonly selectedCount = computed(() => this.selectedIds().size);

  protected setSearch(value: string): void {
    this.search.set(value);
  }

  protected isSelected(id: string): boolean {
    return this.selectedIds().has(id);
  }

  protected toggle(id: string): void {
    this.selectedIds.update((set) => {
      const next = new Set(set);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  protected onClose(): void {
    this.close.emit();
  }

  protected onCreate(): void {
    const ids = this.selectedIds();
    if (ids.size === 0) {
      return;
    }
    const participants = this.candidates().filter((c) => ids.has(c.id));
    this.create.emit({
      participants,
      groupName: participants.length > 1 ? this.groupName().trim() || undefined : undefined,
    });
  }
}
