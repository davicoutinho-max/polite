import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { UiIcon } from '../ui-icon/ui-icon';
import { TranslatePipe } from '../../pipes/translate.pipe';

export interface UiTab {
  readonly id: string;
  readonly label: string;
  /** Translation-tag key resolved against the current language; falls back to `label`. */
  readonly key?: string;
  readonly icon?: string;
}

/**
 * Generic segmented tab control. Selection is controlled by the parent.
 *
 * @example
 * <ui-tabs [tabs]="tabs" [active]="active()" (activeChange)="active.set($event)" />
 */
@Component({
  selector: 'ui-tabs',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon, TranslatePipe],
  template: `
    <div class="tabs" role="tablist">
      @for (tab of tabs(); track tab.id) {
        <button
          type="button"
          role="tab"
          class="tab"
          [class.tab--active]="tab.id === active()"
          [attr.aria-selected]="tab.id === active()"
          (click)="activeChange.emit(tab.id)"
        >
          @if (tab.icon; as ic) {
            <ui-icon [name]="ic" [size]="18" [fill]="tab.id === active()" />
          }
          <span>{{ (tab.key ?? '') | translate: tab.label }}</span>
        </button>
      }
    </div>
  `,
  styles: `
    :host { display: block; }
    .tabs {
      display: inline-flex;
      gap: var(--cp-space-xs);
      padding: var(--cp-space-xs);
      background: var(--cp-surface-container);
      border-radius: var(--cp-radius-full);
      max-width: 100%;
      overflow-x: auto;
    }
    .tab {
      display: inline-flex;
      align-items: center;
      gap: var(--cp-space-xs);
      padding: var(--cp-space-sm) var(--cp-space-lg);
      border: none;
      background: transparent;
      border-radius: var(--cp-radius-full);
      font-family: inherit;
      font-size: 14px;
      font-weight: 600;
      color: var(--cp-on-surface-variant);
      cursor: pointer;
      white-space: nowrap;
      transition: background 0.15s ease, color 0.15s ease;
    }
    .tab:hover { color: var(--cp-on-surface); }
    .tab--active {
      background: var(--cp-surface-container-lowest);
      color: var(--cp-secondary);
      box-shadow: var(--cp-shadow-card);
    }
  `,
})
export class UiTabs {
  readonly tabs = input.required<readonly UiTab[]>();
  readonly active = input.required<string>();
  readonly activeChange = output<string>();
}
