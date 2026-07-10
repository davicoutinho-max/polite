import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/**
 * Generic Material Symbols icon.
 *
 * @example
 * <ui-icon name="verified" [fill]="true" [size]="16" />
 */
@Component({
  selector: 'ui-icon',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="material-symbols-outlined" [style]="style()">{{ name() }}</span>`,
  styles: `
    :host {
      display: inline-flex;
      line-height: 0;
    }
  `,
})
export class UiIcon {
  /** Material Symbols glyph name, e.g. "home", "verified". */
  readonly name = input.required<string>();
  /** Filled vs outlined variant. */
  readonly fill = input(false);
  /** Optical size in px. */
  readonly size = input(24);
  /** Font weight (100–700). */
  readonly weight = input(400);

  protected readonly style = computed(
    () =>
      `font-size:${this.size()}px;` +
      `font-variation-settings:'FILL' ${this.fill() ? 1 : 0},'wght' ${this.weight()},'GRAD' 0,'opsz' ${this.size()};`,
  );
}
