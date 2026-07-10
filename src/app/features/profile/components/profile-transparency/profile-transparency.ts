import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TransparencyReport } from '../../../../core/models';
import { UiSection } from '../../../../shared/ui/ui-section/ui-section';
import { UiStat } from '../../../../shared/ui/ui-stat/ui-stat';
import { UiProgress } from '../../../../shared/ui/ui-progress/ui-progress';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

/** Transparency tab: public-money KPIs and an expense breakdown. */
@Component({
  selector: 'app-profile-transparency',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiSection, UiStat, UiProgress, UiIcon, TranslatePipe],
  template: `
    <div class="transparency">
      <div class="transparency__note">
        <ui-icon name="verified" [fill]="true" [size]="18" />
        <span>{{ 'label.public-data' | translate: 'Public data' }} · {{ report().lastUpdate }}</span>
      </div>

      <div class="metrics">
        @for (metric of report().metrics; track metric.id) {
          <ui-stat [icon]="metric.icon" [label]="metric.label" [value]="metric.value" [caption]="metric.caption" />
        }
      </div>

      <ui-section [title]="'section.expense-breakdown' | translate: 'Expense breakdown'" icon="pie_chart">
        <div class="expense__total">
          <span>{{ 'label.total-reported-expenses' | translate: 'Total reported expenses' }}</span>
          <strong>{{ report().totalExpense }}</strong>
        </div>
        <ul class="expenses">
          @for (line of report().expenses; track line.category) {
            <li class="expense">
              <div class="expense__row">
                <span>{{ line.category }}</span>
                <span class="expense__amount">R$ {{ line.amount.toLocaleString('pt-BR') }}</span>
              </div>
              <ui-progress [value]="line.share" color="secondary" />
            </li>
          }
        </ul>
      </ui-section>
    </div>
  `,
  styles: `
    :host { display: block; }
    .transparency { display: flex; flex-direction: column; gap: var(--cp-space-lg); }
    .transparency__note {
      display: inline-flex; align-items: center; gap: var(--cp-space-xs); width: max-content;
      padding: var(--cp-space-xs) var(--cp-space-md); border-radius: var(--cp-radius-full);
      background: var(--cp-success-container); color: var(--cp-on-success-container);
      font-size: 12px; font-weight: 600; letter-spacing: 0.03em;
    }
    .metrics {
      display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: var(--cp-space-md);
    }
    .expense__total {
      display: flex; justify-content: space-between; align-items: baseline;
      margin-bottom: var(--cp-space-md); padding-bottom: var(--cp-space-md);
      border-bottom: 1px solid var(--cp-outline-variant);
      color: var(--cp-on-surface-variant); font-size: 14px;
    }
    .expense__total strong { font-size: 24px; color: var(--cp-on-surface); }
    .expenses { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: var(--cp-space-md); }
    .expense__row { display: flex; justify-content: space-between; font-size: 14px; margin-bottom: var(--cp-space-xs); color: var(--cp-on-surface); }
    .expense__amount { font-weight: 600; color: var(--cp-on-surface-variant); }
  `,
})
export class ProfileTransparency {
  readonly report = input.required<TransparencyReport>();
}
