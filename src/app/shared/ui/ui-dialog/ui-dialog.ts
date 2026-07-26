import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { UiIcon } from '../ui-icon/ui-icon';

/**
 * Generic centered modal overlay. Content is projected; the header (title + close button) is
 * built in, matching the design system's other popover chrome (see messages-page's thread menu).
 *
 * @example
 * <ui-dialog [open]="showDetails()" title="Petition details" (close)="showDetails.set(false)">
 *   …
 * </ui-dialog>
 */
@Component({
  selector: 'ui-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon],
  template: `
    @if (open()) {
      <div class="ui-dialog__backdrop" (click)="onBackdropClick()">
        <div class="ui-dialog__panel" [style.maxWidth.px]="maxWidth()" role="dialog" aria-modal="true" (click)="$event.stopPropagation()">
          <div class="ui-dialog__header">
            <h3>{{ title() }}</h3>
            <button type="button" class="ui-dialog__close" [attr.aria-label]="closeLabel()" (click)="close.emit()">
              <ui-icon name="close" [size]="20" />
            </button>
          </div>
          <div class="ui-dialog__body">
            <ng-content />
          </div>
        </div>
      </div>
    }
  `,
  styles: `
    :host { display: contents; }
    .ui-dialog__backdrop {
      position: fixed; inset: 0; z-index: 100;
      background: rgba(0, 0, 0, 0.5);
      display: flex; align-items: center; justify-content: center;
      padding: var(--cp-space-lg);
    }
    .ui-dialog__panel {
      width: 100%;
      max-height: min(85vh, 720px);
      overflow-y: auto;
      background: var(--cp-surface-container-lowest);
      border-radius: var(--cp-radius-lg);
      box-shadow: var(--cp-shadow-hover);
      display: flex;
      flex-direction: column;
    }
    .ui-dialog__header {
      display: flex; align-items: center; justify-content: space-between; gap: var(--cp-space-md);
      padding: var(--cp-space-lg) var(--cp-space-lg) var(--cp-space-md);
      border-bottom: 1px solid var(--cp-outline-variant);
      position: sticky; top: 0; background: var(--cp-surface-container-lowest); z-index: 1;
    }
    .ui-dialog__header h3 { margin: 0; font-size: 18px; font-weight: 600; color: var(--cp-on-surface); }
    .ui-dialog__close {
      display: inline-flex; align-items: center; justify-content: center;
      border: none; background: transparent; color: var(--cp-on-surface-variant);
      border-radius: var(--cp-radius-full); padding: var(--cp-space-xs); cursor: pointer;
      transition: background 0.2s ease;
    }
    .ui-dialog__close:hover { background: var(--cp-surface-container-low); }
    .ui-dialog__body { padding: var(--cp-space-lg); }
  `,
})
export class UiDialog {
  readonly open = input(false);
  readonly title = input('');
  readonly maxWidth = input(520);
  readonly dismissible = input(true);
  readonly closeLabel = input('Close');
  readonly close = output<void>();

  protected onBackdropClick(): void {
    if (this.dismissible()) {
      this.close.emit();
    }
  }
}
