import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { ProfileTab } from '../../../../core/models';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

/** Generic, horizontally scrollable tab bar. Selection is controlled by the parent. */
@Component({
  selector: 'app-profile-tabs',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon, TranslatePipe],
  template: `
    <div class="tabs">
      <div class="tabs__inner">
        @for (tab of tabs(); track tab.id) {
          <button
            type="button"
            class="tab"
            [class.tab--active]="tab.id === activeId()"
            (click)="tabChange.emit(tab.id)"
          >
            <ui-icon [name]="tab.icon" [size]="18" [fill]="tab.id === activeId()" />
            <span>{{ tab.key | translate: tab.label }}</span>
          </button>
        }
      </div>
    </div>
  `,
  styles: `
    .tabs {
      border-bottom: 1px solid var(--cp-outline-variant);
      overflow-x: auto;
    }
    .tabs__inner {
      display: flex;
      gap: var(--cp-space-lg);
      min-width: max-content;
    }
    .tab {
      display: inline-flex;
      align-items: center;
      gap: var(--cp-space-xs);
      background: transparent;
      border: none;
      border-bottom: 2px solid transparent;
      padding: 0 var(--cp-space-xs) var(--cp-space-sm);
      font-family: inherit;
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 0.05em;
      color: var(--cp-on-surface-variant);
      cursor: pointer;
      transition: color 0.2s ease, border-color 0.2s ease;
      white-space: nowrap;
    }
    .tab:hover { color: var(--cp-secondary); }
    .tab--active {
      color: var(--cp-secondary);
      font-weight: 700;
      border-bottom-color: var(--cp-secondary);
    }
  `,
})
export class ProfileTabs {
  readonly tabs = input.required<readonly ProfileTab[]>();
  readonly activeId = input.required<string>();
  readonly tabChange = output<string>();
}
