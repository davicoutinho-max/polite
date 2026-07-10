import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { CareerMilestone } from '../../../../core/models';
import { UiSection } from '../../../../shared/ui/ui-section/ui-section';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

/** Career timeline tab: chronological year-based milestones. */
@Component({
  selector: 'app-profile-career',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiSection, TranslatePipe],
  template: `
    <ui-section [title]="'section.career-timeline' | translate: 'Career timeline'" icon="timeline">
      <ol class="career">
        @for (milestone of milestones(); track milestone.year; let last = $last) {
          <li class="milestone" [class.milestone--last]="last">
            <div class="milestone__marker">
              <span class="milestone__year">{{ milestone.year }}</span>
            </div>
            <div class="milestone__content">
              <div class="milestone__title">{{ milestone.title }}</div>
              @if (milestone.detail; as d) {
                <div class="milestone__detail">{{ d }}</div>
              }
            </div>
          </li>
        }
      </ol>
    </ui-section>
  `,
  styles: `
    :host { display: block; }
    .career { list-style: none; margin: 0; padding: 0; }
    .milestone { position: relative; display: flex; gap: var(--cp-space-lg); padding-bottom: var(--cp-space-lg); }
    .milestone::before {
      content: ''; position: absolute; left: 27px; top: 32px; bottom: 0;
      width: 2px; background: var(--cp-outline-variant);
    }
    .milestone--last { padding-bottom: 0; }
    .milestone--last::before { display: none; }
    .milestone__marker {
      flex-shrink: 0; width: 56px; height: 56px; border-radius: var(--cp-radius-full);
      background: var(--cp-secondary-container); color: var(--cp-on-secondary-container);
      display: flex; align-items: center; justify-content: center; z-index: 1;
    }
    .milestone__year { font-size: 14px; font-weight: 700; }
    .milestone__content { padding-top: var(--cp-space-sm); }
    .milestone__title { font-size: 18px; font-weight: 600; color: var(--cp-on-surface); }
    .milestone__detail { font-size: 14px; color: var(--cp-on-surface-variant); margin-top: 2px; }
  `,
})
export class ProfileCareer {
  readonly milestones = input.required<readonly CareerMilestone[]>();
}
