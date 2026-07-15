import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AssistantMessage, AssistantPrompt, AssistantPromptKind, AssistantTopic } from '../models';

interface AssistantTopicResponseDto {
  readonly id: string;
  readonly reference: string;
  readonly title: string;
  readonly answers: Record<string, string>;
}

/**
 * Civic assistant — canned answers stored and served by assistant-service (DB-backed, not
 * LLM-backed; see the service's own scope note). Every response is deterministic and clearly
 * framed as an AI-generated explanation, never legal advice.
 */
@Injectable({ providedIn: 'root' })
export class AssistantService {
  private readonly http = inject(HttpClient);
  private readonly apiBase = `${environment.apiBaseUrl}/api/assistant`;

  readonly prompts: AssistantPrompt[] = [
    { kind: 'summary', label: 'Summarize', icon: 'summarize' },
    { kind: 'plain', label: 'Explain simply', icon: 'lightbulb' },
    { kind: 'impact', label: 'What changes if approved?', icon: 'trending_up' },
  ];

  private readonly _topics = signal<AssistantTopic[]>([]);
  readonly topics = this._topics.asReadonly();

  private readonly _conversation = signal<AssistantMessage[]>([]);
  readonly conversation = this._conversation.asReadonly();

  private readonly _thinking = signal(false);
  readonly thinking = this._thinking.asReadonly();

  constructor() {
    this.reload().subscribe();
  }

  reload(): Observable<AssistantTopic[]> {
    return this.http.get<AssistantTopicResponseDto[]>(`${this.apiBase}/topics`).pipe(
      map((list) =>
        list.map(
          (dto): AssistantTopic => ({
            id: dto.id,
            reference: dto.reference,
            title: dto.title,
            answers: {
              summary: dto.answers['summary'] ?? '',
              plain: dto.answers['plain'] ?? '',
              impact: dto.answers['impact'] ?? '',
            },
          }),
        ),
      ),
      tap((topics) => this._topics.set(topics)),
    );
  }

  /** Ask the assistant to apply a lens to a topic. Simulates a short delay before showing the
   * already-loaded canned answer — there is no live model round-trip to wait for. */
  ask(topic: AssistantTopic, kind: AssistantPromptKind, promptLabel: string): void {
    const userMsg: AssistantMessage = {
      id: `u${Date.now()}`,
      role: 'user',
      text: `${promptLabel} — ${topic.reference}: ${topic.title}`,
      topicReference: topic.reference,
    };
    this._conversation.update((c) => [...c, userMsg]);
    this._thinking.set(true);

    setTimeout(() => {
      const answer: AssistantMessage = {
        id: `a${Date.now()}`,
        role: 'assistant',
        text: topic.answers[kind],
        topicReference: topic.reference,
      };
      this._conversation.update((c) => [...c, answer]);
      this._thinking.set(false);
    }, 700);
  }

  clear(): void {
    this._conversation.set([]);
  }
}
