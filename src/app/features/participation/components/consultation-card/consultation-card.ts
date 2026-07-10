import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { Consultation, ConsultationStance } from '../../../../core/models';
import { CompactNumberPipe } from '../../../../shared/pipes/compact-number.pipe';
import { UiCard } from '../../../../shared/ui/ui-card/ui-card';
import { UiTag } from '../../../../shared/ui/ui-tag/ui-tag';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

interface StanceEvent {
  readonly id: string;
  readonly stance: ConsultationStance;
}

/** Public consultation card — the citizen registers a stance. */
@Component({
  selector: 'app-consultation-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiCard, UiTag, UiIcon, CompactNumberPipe, TranslatePipe],
  template: `
    <ui-card [hover]="true" padding="lg">
      <div class="head">
        <ui-tag [label]="consultation().status.label" [severity]="consultation().status.severity" />
        <span class="responses">
          <ui-icon name="forum" [size]="16" />
          {{ consultation().responses | compactNumber }} {{ 'label.responses' | translate: 'responses' }}
        </span>
      </div>
      <h3 class="title">{{ consultation().title }}</h3>
      <p class="desc">{{ consultation().description }}</p>

      <div class="stances">
        <button
          type="button"
          class="stance stance--favor"
          [class.stance--picked]="consultation().stance === 'favor'"
          (click)="pick('favor')"
        >
          <ui-icon name="thumb_up" [size]="18" [fill]="consultation().stance === 'favor'" /> {{ 'label.in-favor' | translate: 'In favor' }}
        </button>
        <button
          type="button"
          class="stance stance--against"
          [class.stance--picked]="consultation().stance === 'against'"
          (click)="pick('against')"
        >
          <ui-icon name="thumb_down" [size]="18" [fill]="consultation().stance === 'against'" /> {{ 'label.against' | translate: 'Against' }}
        </button>
        <button
          type="button"
          class="stance stance--neutral"
          [class.stance--picked]="consultation().stance === 'neutral'"
          (click)="pick('neutral')"
        >
          <ui-icon name="drag_handle" [size]="18" /> {{ 'label.neutral' | translate: 'Neutral' }}
        </button>
      </div>

      <div class="foot">
        <span class="deadline">{{ consultation().deadline }}</span>
        @if (consultation().stance) {
          <span class="registered"><ui-icon name="check_circle" [size]="16" [fill]="true" /> {{ 'hint.stance-recorded' | translate: 'Your stance was recorded' }}</span>
        }
      </div>
    </ui-card>
  `,
  styles: `
    :host { display: block; }
    .head { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--cp-space-md); gap: var(--cp-space-sm); }
    .responses { display: inline-flex; align-items: center; gap: var(--cp-space-xs); font-size: 13px; color: var(--cp-on-surface-variant); }
    .title { margin: 0 0 var(--cp-space-sm); font-size: 20px; line-height: 28px; font-weight: 600; color: var(--cp-on-surface); }
    .desc { margin: 0 0 var(--cp-space-lg); font-size: 14px; line-height: 20px; color: var(--cp-on-surface-variant); }
    .stances { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--cp-space-sm); }
    .stance {
      display: inline-flex; align-items: center; justify-content: center; gap: var(--cp-space-xs);
      padding: var(--cp-space-sm); border-radius: var(--cp-radius);
      border: 1px solid var(--cp-outline-variant); background: var(--cp-surface);
      font-family: inherit; font-size: 13px; font-weight: 600; color: var(--cp-on-surface-variant);
      cursor: pointer; transition: all 0.15s ease;
    }
    .stance:hover { border-color: var(--cp-secondary); color: var(--cp-secondary); }
    .stance--favor.stance--picked { background: var(--cp-success-container); color: var(--cp-on-success-container); border-color: transparent; }
    .stance--against.stance--picked { background: var(--cp-danger-container); color: var(--cp-on-danger-container); border-color: transparent; }
    .stance--neutral.stance--picked { background: var(--cp-surface-container-high); color: var(--cp-on-surface); border-color: transparent; }
    .foot {
      display: flex; justify-content: space-between; align-items: center; gap: var(--cp-space-md);
      margin-top: var(--cp-space-md); padding-top: var(--cp-space-md);
      border-top: 1px solid var(--cp-outline-variant); flex-wrap: wrap;
    }
    .deadline { font-size: 12px; font-weight: 600; letter-spacing: 0.03em; color: var(--cp-on-surface-variant); }
    .registered { display: inline-flex; align-items: center; gap: var(--cp-space-xs); font-size: 12px; font-weight: 600; color: var(--cp-on-success-container); }
  `,
})
export class ConsultationCard {
  readonly consultation = input.required<Consultation>();
  readonly stanceChange = output<StanceEvent>();

  protected pick(stance: ConsultationStance): void {
    this.stanceChange.emit({ id: this.consultation().id, stance });
  }
}
