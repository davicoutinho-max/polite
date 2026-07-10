import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { TagSeverity } from '../../../core/models';
import { UiIcon } from '../ui-icon/ui-icon';

/**
 * Generic status chip. Colour language is driven entirely by `severity`, so
 * every chip in the app (post tags, bill status, party badges) stays consistent.
 *
 * @example <ui-tag label="Passed" severity="success" [uppercase]="true" />
 */
@Component({
  selector: 'ui-tag',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon],
  template: `
    @if (icon(); as ic) {
      <ui-icon [name]="ic" [size]="14" [fill]="true" />
    }
    <span>{{ label() }}</span>
  `,
  host: {
    '[class]': 'hostClass()',
  },
  styles: `
    :host {
      display: inline-flex;
      align-items: center;
      gap: var(--cp-space-xs);
      padding: 3px 10px;
      border-radius: var(--cp-radius-full);
      font-size: 12px;
      line-height: 16px;
      font-weight: 600;
      letter-spacing: 0.02em;
      white-space: nowrap;
    }
    :host(.ui-tag--upper) {
      text-transform: uppercase;
      font-size: 10px;
      letter-spacing: 0.05em;
      padding: 3px 8px;
    }
    :host(.ui-tag--primary)   { background: var(--cp-primary-container); color: var(--cp-on-primary); }
    :host(.ui-tag--secondary) { background: var(--cp-surface-container-highest); color: var(--cp-secondary); }
    :host(.ui-tag--success)   { background: var(--cp-success-container); color: var(--cp-on-success-container); }
    :host(.ui-tag--danger)    { background: var(--cp-danger-container); color: var(--cp-on-danger-container); }
    :host(.ui-tag--warning)   { background: var(--cp-tertiary-fixed-dim); color: var(--cp-on-tertiary-fixed-variant); }
    :host(.ui-tag--info)      { background: var(--cp-secondary-container); color: var(--cp-on-secondary-container); }
    :host(.ui-tag--neutral)   { background: var(--cp-surface-container-high); color: var(--cp-on-surface-variant); }
  `,
})
export class UiTag {
  readonly label = input.required<string>();
  readonly severity = input<TagSeverity>('neutral');
  readonly icon = input<string>();
  readonly uppercase = input(false);

  protected readonly hostClass = computed(
    () => `ui-tag ui-tag--${this.severity()}${this.uppercase() ? ' ui-tag--upper' : ''}`,
  );
}
