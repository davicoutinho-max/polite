import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { Survey } from '../../../../core/models';
import { CompactNumberPipe } from '../../../../shared/pipes/compact-number.pipe';
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
  imports: [UiCard, UiIcon, CompactNumberPipe, TranslatePipe],
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
        <ui-icon name="how_to_vote" [size]="16" />
        {{ total() | compactNumber }} {{ 'label.votes' | translate: 'votes' }}{{ voted() ? ' · ' + ('hint.thanks-for-voting' | translate: 'thanks for voting') : '' }}
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
      display: inline-flex; align-items: center; gap: var(--cp-space-xs);
      margin-top: var(--cp-space-md); font-size: 13px; color: var(--cp-on-surface-variant);
    }
  `,
})
export class SurveyCard {
  readonly survey = input.required<Survey>();
  readonly vote = output<VoteEvent>();

  protected readonly voted = computed(() => this.survey().votedOptionId !== null);
  protected readonly total = computed(() => this.survey().options.reduce((sum, o) => sum + o.votes, 0));

  protected percent(votes: number): number {
    const total = this.total();
    return total === 0 ? 0 : Math.round((votes / total) * 100);
  }
}
