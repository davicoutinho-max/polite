import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ThemeService } from '../../core/services/theme.service';
import { UiIconButton } from '../../shared/ui/ui-icon-button/ui-icon-button';

/** Light/dark theme switch. */
@Component({
  selector: 'app-theme-toggle',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIconButton],
  template: `
    <ui-icon-button
      [icon]="theme.isDark() ? 'light_mode' : 'dark_mode'"
      [ariaLabel]="theme.isDark() ? 'Switch to light mode' : 'Switch to dark mode'"
      [iconSize]="iconSize()"
      (click)="theme.toggle()"
    />
  `,
  host: { class: 'theme-toggle' },
})
export class ThemeToggle {
  protected readonly theme = inject(ThemeService);
  protected iconSize(): number {
    return 22;
  }
}
