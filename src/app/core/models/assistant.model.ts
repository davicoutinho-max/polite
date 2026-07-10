/** The three canned lenses the assistant can apply to a piece of legislation. */
export type AssistantPromptKind = 'summary' | 'plain' | 'impact';

export interface AssistantPrompt {
  readonly kind: AssistantPromptKind;
  readonly label: string;
  readonly icon: string;
}

/** A subject the assistant can reason about (a bill, PEC, CPI, etc.). */
export interface AssistantTopic {
  readonly id: string;
  readonly reference: string;
  readonly title: string;
  /** Pre-written answers keyed by prompt kind. */
  readonly answers: Record<AssistantPromptKind, string>;
}

export interface AssistantMessage {
  readonly id: string;
  readonly role: 'user' | 'assistant';
  readonly text: string;
  readonly topicReference?: string;
}
