import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { Petition } from '../../../../core/models';
import { CompactNumberPipe } from '../../../../shared/pipes/compact-number.pipe';
import { UiCard } from '../../../../shared/ui/ui-card/ui-card';
import { UiTag } from '../../../../shared/ui/ui-tag/ui-tag';
import { UiButton } from '../../../../shared/ui/ui-button/ui-button';
import { UiProgress } from '../../../../shared/ui/ui-progress/ui-progress';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

/** Petition (abaixo-assinado) card with signature progress and a sign action. */
@Component({
  selector: 'app-petition-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiCard, UiTag, UiButton, UiProgress, CompactNumberPipe, TranslatePipe],
  template: `
    <ui-card [hover]="true" padding="lg">
      <div class="head">
        <ui-tag [label]="petition().category" severity="neutral" />
        <ui-tag [label]="petition().status.label" [severity]="petition().status.severity" />
      </div>
      <h3 class="title">{{ petition().title }}</h3>
      <p class="summary">{{ petition().summary }}</p>

      <div class="progress">
        <div class="progress__row">
          <span class="progress__count">{{ petition().signatures | compactNumber }} {{ 'label.signatures' | translate: 'signatures' }}</span>
          <span class="progress__goal">{{ 'label.goal' | translate: 'Goal' }} {{ petition().goal | compactNumber }}</span>
        </div>
        <ui-progress [value]="percent()" [color]="percent() >= 100 ? 'tertiary' : 'secondary'" />
      </div>

      <div class="foot">
        <span class="deadline">{{ petition().deadline }}</span>
        @if (petition().signed) {
          <ui-button [label]="'label.signed' | translate: 'Signed'" icon="check" variant="tonal" [disabled]="true" />
        } @else {
          <ui-button [label]="'button.sign-petition' | translate: 'Sign petition'" icon="edit_document" variant="filled" (click)="sign.emit(petition().id)" />
        }
      </div>
    </ui-card>
  `,
  styles: `
    :host { display: block; }
    .head { display: flex; gap: var(--cp-space-sm); margin-bottom: var(--cp-space-md); flex-wrap: wrap; }
    .title { margin: 0 0 var(--cp-space-sm); font-size: 20px; line-height: 28px; font-weight: 600; color: var(--cp-on-surface); }
    .summary { margin: 0 0 var(--cp-space-lg); font-size: 14px; line-height: 20px; color: var(--cp-on-surface-variant); }
    .progress { margin-bottom: var(--cp-space-md); }
    .progress__row { display: flex; justify-content: space-between; margin-bottom: var(--cp-space-xs); }
    .progress__count { font-weight: 700; color: var(--cp-on-surface); }
    .progress__goal { font-size: 14px; color: var(--cp-on-surface-variant); }
    .foot {
      display: flex; justify-content: space-between; align-items: center; gap: var(--cp-space-md);
      margin-top: var(--cp-space-md); padding-top: var(--cp-space-md);
      border-top: 1px solid var(--cp-outline-variant); flex-wrap: wrap;
    }
    .deadline { font-size: 12px; font-weight: 600; letter-spacing: 0.03em; color: var(--cp-on-surface-variant); }
  `,
})
export class PetitionCard {
  readonly petition = input.required<Petition>();
  readonly sign = output<string>();

  protected readonly percent = computed(() =>
    Math.min(100, Math.round((this.petition().signatures / this.petition().goal) * 100)),
  );
}
