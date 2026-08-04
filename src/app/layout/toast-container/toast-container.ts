import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AlertsService } from '../../core/services/alerts.service';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';

/** Floating success/error feedback for the current user's own just-taken action — see
 * AlertsService.push's javadoc for why this exists (the bell-icon dropdown alone was easy to
 * miss, especially right after clicking a Save button at the bottom of a long form). Mounted
 * once in Shell, fixed-position, so it stays visible regardless of scroll position or route. */
@Component({
  selector: 'app-toast-container',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon],
  template: `
    <div class="toasts" role="status" aria-live="polite">
      @for (toast of alerts.toasts(); track toast.id) {
        <div class="toast" [class.toast--success]="isSuccess(toast.icon)" [class.toast--error]="isError(toast.icon)">
          <ui-icon [name]="toast.icon" [size]="20" [fill]="true" />
          <div class="toast__body">
            <span class="toast__title">{{ toast.title }}</span>
            <span class="toast__message">{{ toast.message }}</span>
          </div>
          <button type="button" class="toast__close" [attr.aria-label]="'Dismiss'" (click)="alerts.dismissToast(toast.id)">
            <ui-icon name="close" [size]="16" />
          </button>
        </div>
      }
    </div>
  `,
  styles: `
    :host { display: block; }
    .toasts {
      position: fixed;
      z-index: 2000;
      right: var(--cp-space-md);
      bottom: var(--cp-space-md);
      display: flex;
      flex-direction: column-reverse;
      gap: var(--cp-space-sm);
      width: min(360px, calc(100vw - var(--cp-space-md) * 2));
      pointer-events: none;
    }
    .toast {
      display: flex;
      align-items: flex-start;
      gap: var(--cp-space-sm);
      padding: var(--cp-space-md);
      border-radius: var(--cp-radius-lg);
      background: var(--cp-surface-container-lowest);
      border: 1px solid var(--cp-outline-variant);
      box-shadow: var(--cp-shadow-hover);
      color: var(--cp-on-surface);
      pointer-events: auto;
      animation: toast-in 0.2s ease;
    }
    .toast--success { border-color: var(--cp-on-success-container); color: var(--cp-on-success-container); background: var(--cp-success-container); }
    .toast--error { border-color: var(--cp-error); color: var(--cp-on-danger-container); background: var(--cp-danger-container); }
    .toast__body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
    .toast__title { font-size: 14px; font-weight: 700; }
    .toast__message { font-size: 13px; opacity: 0.9; overflow-wrap: anywhere; }
    .toast__close {
      flex-shrink: 0;
      border: none;
      background: transparent;
      color: inherit;
      opacity: 0.6;
      cursor: pointer;
      padding: 2px;
      border-radius: var(--cp-radius-full);
    }
    .toast__close:hover { opacity: 1; }
    @keyframes toast-in {
      from { opacity: 0; transform: translateY(8px); }
      to { opacity: 1; transform: translateY(0); }
    }
  `,
})
export class ToastContainer {
  protected readonly alerts = inject(AlertsService);

  protected isSuccess(icon: string): boolean {
    return icon === 'check_circle';
  }

  protected isError(icon: string): boolean {
    return icon === 'error';
  }
}
