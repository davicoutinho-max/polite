import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Bill } from '../../../../core/models';
import { UiButton } from '../../../../shared/ui/ui-button/ui-button';
import { UiSection } from '../../../../shared/ui/ui-section/ui-section';
import { UiTag } from '../../../../shared/ui/ui-tag/ui-tag';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

/** Sidebar widget listing relevant bills with their legislative status. */
@Component({
  selector: 'app-relevant-bills',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiSection, UiTag, UiButton, UiIcon, RouterLink, TranslatePipe],
  template: `
    <ui-section [title]="'section.relevant-bills' | translate: 'Relevant Bills'" icon="gavel">
      <ul class="bills">
        @for (bill of bills(); track bill.id) {
          <li class="bill" [attr.data-severity]="bill.status.severity" [routerLink]="['/profile', bill.politicianId]">
            <div class="bill__top">
              <span class="bill__ref">
                <ui-icon name="description" [size]="14" />
                {{ bill.reference }}
              </span>
              <ui-tag
                [label]="bill.status.label"
                [severity]="bill.status.severity"
                [uppercase]="!!bill.status.uppercase"
              />
            </div>
            <div class="bill__title">{{ bill.title }}</div>
            @if (bill.sponsor) {
              <div class="bill__sponsor">{{ bill.sponsor }}</div>
            }
          </li>
        }
      </ul>
      <div class="widget__footer">
        <ui-button [label]="'button.view-all-active-bills' | translate: 'View All Active Bills'" variant="text" routerLink="/politicians" />
      </div>
    </ui-section>
  `,
  styles: `
    :host { display: block; }
    .bills { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: var(--cp-space-md); }
    .bill {
      position: relative;
      display: flex;
      flex-direction: column;
      gap: var(--cp-space-xs);
      border: 1px solid var(--cp-outline-variant);
      border-radius: var(--cp-radius-lg);
      background: var(--cp-surface);
      padding: var(--cp-space-md);
      padding-left: calc(var(--cp-space-md) + 3px);
      cursor: pointer;
      overflow: hidden;
      transition: box-shadow 0.15s ease, border-color 0.15s ease, transform 0.15s ease;
    }
    .bill::before {
      content: '';
      position: absolute;
      left: 0; top: 0; bottom: 0;
      width: 4px;
      background: var(--cp-outline-variant);
    }
    .bill[data-severity='success']::before { background: var(--cp-on-success-container); }
    .bill[data-severity='danger']::before { background: var(--cp-on-danger-container); }
    .bill[data-severity='warning']::before { background: var(--cp-on-tertiary-container); }
    .bill[data-severity='secondary']::before { background: var(--cp-secondary); }
    .bill:hover {
      box-shadow: var(--cp-shadow-hover);
      border-color: var(--cp-secondary-fixed-dim);
      transform: translateY(-1px);
    }
    .bill__top { display: flex; justify-content: space-between; align-items: center; gap: var(--cp-space-sm); }
    .bill__ref {
      display: inline-flex; align-items: center; gap: var(--cp-space-xs);
      font-size: 12px; font-weight: 700; letter-spacing: 0.03em; color: var(--cp-secondary);
    }
    .bill__title { font-size: 15px; font-weight: 600; color: var(--cp-on-surface); line-height: 20px; }
    .bill__sponsor { font-size: 12px; color: var(--cp-on-surface-variant); }
    .widget__footer { display: flex; justify-content: center; margin-top: var(--cp-space-md); }
  `,
})
export class RelevantBills {
  readonly bills = input.required<readonly Bill[]>();
}
