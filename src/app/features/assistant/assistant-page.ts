import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { AssistantService } from '../../core/services/assistant.service';
import { AssistantPromptKind } from '../../core/models';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Civic AI assistant: summarize / explain / project the impact of legislation. */
@Component({
  selector: 'app-assistant-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeader, UiIcon, TranslatePipe],
  templateUrl: './assistant-page.html',
  styleUrl: './assistant-page.scss',
})
export class AssistantPage {
  private readonly assistant = inject(AssistantService);

  protected readonly topics = this.assistant.topics;
  protected readonly prompts = this.assistant.prompts;
  protected readonly conversation = this.assistant.conversation;
  protected readonly thinking = this.assistant.thinking;

  protected readonly activeTopicId = signal(this.topics[0]?.id ?? '');
  protected readonly activeTopic = computed(
    () => this.topics.find((t) => t.id === this.activeTopicId()) ?? this.topics[0],
  );

  protected selectTopic(id: string): void {
    this.activeTopicId.set(id);
  }

  protected ask(kind: AssistantPromptKind, label: string): void {
    const topic = this.activeTopic();
    if (topic) {
      this.assistant.ask(topic, kind, label);
    }
  }

  protected clear(): void {
    this.assistant.clear();
  }
}
