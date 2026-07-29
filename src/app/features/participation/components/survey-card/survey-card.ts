import { ChangeDetectionStrategy, Component, computed, inject, input, output } from '@angular/core';
import { Observable } from 'rxjs';
import { Survey } from '../../../../core/models';
import { AiAssistantService } from '../../../../core/services/ai-assistant.service';
import { TranslateService } from '../../../../core/services/translate.service';
import { CompactNumberPipe } from '../../../../shared/pipes/compact-number.pipe';
import { AskAi, AskAiPromptOption } from '../../../../shared/ai/ask-ai/ask-ai';
import { UiCard } from '../../../../shared/ui/ui-card/ui-card';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

interface VoteEvent {
  readonly surveyId: string;
  readonly optionId: string;
}

/** Survey / poll card. Shows results after the citizen votes. */
@Component({
  selector: 'app-survey-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiCard, UiIcon, AskAi, CompactNumberPipe, TranslatePipe],
  template: `
    <ui-card padding="lg">
      <div class="context">{{ survey().context }}</div>
      <h3 class="question">{{ survey().question }}</h3>

      <ul class="options">
        @for (option of survey().options; track option.id) {
          <li>
            @if (voted()) {
              <div class="result" [class.result--picked]="option.id === survey().votedOptionId">
                <div class="result__bar" [style.width.%]="percent(option.votes)"></div>
                <div class="result__content">
                  <span class="result__label">
                    @if (option.id === survey().votedOptionId) {
                      <ui-icon name="check_circle" [size]="16" [fill]="true" />
                    }
                    {{ option.label }}
                  </span>
                  <span class="result__pct">{{ percent(option.votes) }}%</span>
                </div>
              </div>
            } @else {
              <button type="button" class="option" (click)="vote.emit({ surveyId: survey().id, optionId: option.id })">
                {{ option.label }}
              </button>
            }
          </li>
        }
      </ul>

      <div class="foot">
        <span class="foot__votes">
          <ui-icon name="how_to_vote" [size]="16" />
          {{ total() | compactNumber }} {{ 'label.votes' | translate: 'votes' }}{{ voted() ? ' · ' + ('hint.thanks-for-voting' | translate: 'thanks for voting') : '' }}
        </span>
        <app-ask-ai [topicLabel]="survey().question" [askFn]="askAiFn()" [prompts]="askAiPrompts" />
      </div>
    </ui-card>
  `,
  styles: `
    :host { display: block; }
    .context { font-size: 12px; font-weight: 600; letter-spacing: 0.03em; color: var(--cp-on-surface-variant); }
    .question { margin: var(--cp-space-xs) 0 var(--cp-space-lg); font-size: 20px; line-height: 28px; font-weight: 600; color: var(--cp-on-surface); }
    .options { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: var(--cp-space-sm); }

    .option {
      width: 100%; text-align: left; padding: var(--cp-space-md);
      border: 1px solid var(--cp-outline-variant); border-radius: var(--cp-radius);
      background: var(--cp-surface); font-family: inherit; font-size: 15px; font-weight: 600;
      color: var(--cp-on-surface); cursor: pointer; transition: all 0.15s ease;
    }
    .option:hover { border-color: var(--cp-secondary); background: var(--cp-surface-container-low); color: var(--cp-secondary); }

    .result {
      position: relative; overflow: hidden;
      border: 1px solid var(--cp-outline-variant); border-radius: var(--cp-radius);
      background: var(--cp-surface);
    }
    .result--picked { border-color: var(--cp-secondary); }
    .result__bar { position: absolute; inset: 0; width: 0; background: var(--cp-surface-container-high); transition: width 0.5s ease; }
    .result--picked .result__bar { background: var(--cp-secondary-container); }
    .result__content { position: relative; display: flex; justify-content: space-between; align-items: center; padding: var(--cp-space-md); }
    .result__label { display: inline-flex; align-items: center; gap: var(--cp-space-xs); font-weight: 600; color: var(--cp-on-surface); }
    .result__pct { font-weight: 700; color: var(--cp-on-surface); }

    .foot {
      display: flex; align-items: center; justify-content: space-between; gap: var(--cp-space-sm);
      margin-top: var(--cp-space-md); flex-wrap: wrap;
    }
    .foot__votes { display: inline-flex; align-items: center; gap: var(--cp-space-xs); font-size: 13px; color: var(--cp-on-surface-variant); }
  `,
})
export class SurveyCard {
  private readonly aiAssistant = inject(AiAssistantService);
  private readonly translate = inject(TranslateService);

  readonly survey = input.required<Survey>();
  readonly vote = output<VoteEvent>();

  protected readonly voted = computed(() => this.survey().votedOptionId !== null);
  protected readonly total = computed(() => this.survey().options.reduce((sum, o) => sum + o.votes, 0));

  protected readonly askAiFn = computed(
    () => (question: string): Observable<string> =>
      this.aiAssistant.askAboutParticipationItem('survey', this.survey().question, this.survey().context, question),
  );
  protected readonly askAiPrompts: AskAiPromptOption[] = [
    {
      question: 'Why might this survey matter to citizens?',
      label: this.translate.t('button.ask-ai-why-matters', 'Why does this matter?'),
      icon: 'lightbulb',
    },
  ];

  protected percent(votes: number): number {
    const total = this.total();
    return total === 0 ? 0 : Math.round((votes / total) * 100);
  }
}
