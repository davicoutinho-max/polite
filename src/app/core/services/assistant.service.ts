import { Injectable, signal } from '@angular/core';
import {
  AssistantMessage,
  AssistantPrompt,
  AssistantPromptKind,
  AssistantTopic,
} from '../models';

/**
 * Mock civic assistant. Answers are pre-written per topic — there is no live
 * model behind it — so every response is deterministic and clearly framed as an
 * AI-generated explanation, never legal advice.
 */
@Injectable({ providedIn: 'root' })
export class AssistantService {
  readonly prompts: AssistantPrompt[] = [
    { kind: 'summary', label: 'Summarize', icon: 'summarize' },
    { kind: 'plain', label: 'Explain simply', icon: 'lightbulb' },
    { kind: 'impact', label: 'What changes if approved?', icon: 'trending_up' },
  ];

  readonly topics: AssistantTopic[] = [
    {
      id: 't1',
      reference: 'PEC 33/2024',
      title: 'Fiscal Transparency Amendment',
      answers: {
        summary:
          'PEC 33/2024 amends the Constitution to require every level of government to publish budget execution data in an open, machine-readable format within 30 days of each transaction, and creates an independent oversight council to audit compliance.',
        plain:
          'Think of it as a rule that forces governments to show, almost in real time, where public money goes — in a format anyone can download and check. It also sets up a watchdog group to make sure they actually do it.',
        impact:
          'If approved: federal, state and municipal bodies would have to open their spending data; citizens and journalists could track contracts faster; and non-compliant officials could face sanctions from the new council. It would take effect 180 days after promulgation.',
      },
    },
    {
      id: 't2',
      reference: 'H.R. 452',
      title: 'Clean Water Infrastructure Act',
      answers: {
        summary:
          'H.R. 452 funds the modernization of municipal water-treatment plants and the replacement of lead service pipes across urban districts over a ten-year horizon, with matching grants for smaller cities.',
        plain:
          'It is a plan to fix old water systems and remove lead pipes so tap water is safer, especially in cities. Bigger cities help pay; smaller ones get extra federal help.',
        impact:
          'If approved: cities could apply for grants starting next fiscal year, lead-pipe replacement would be prioritized in high-risk zones, and water utilities would report progress annually. Households in affected areas should see reduced contamination risk within 3–5 years.',
      },
    },
    {
      id: 't3',
      reference: 'CPI 05/2023',
      title: 'Public Contracts Inquiry',
      answers: {
        summary:
          'CPI 05/2023 is a parliamentary commission of inquiry investigating irregularities in public procurement contracts signed between 2020 and 2022, with power to summon witnesses and request documents.',
        plain:
          'It is a formal investigation by lawmakers into whether some government contracts were awarded unfairly. They can call people to testify and demand paperwork.',
        impact:
          'If its findings are approved: the commission may refer cases to prosecutors, recommend new procurement rules, and publish a public report. It does not itself convict anyone — it investigates and recommends.',
      },
    },
  ];

  private readonly _conversation = signal<AssistantMessage[]>([]);
  readonly conversation = this._conversation.asReadonly();

  private readonly _thinking = signal(false);
  readonly thinking = this._thinking.asReadonly();

  /** Ask the assistant to apply a lens to a topic. Simulates a short delay. */
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
