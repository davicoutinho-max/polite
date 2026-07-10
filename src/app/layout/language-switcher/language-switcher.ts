import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { LocaleService } from '../../core/services/locale.service';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';

/** Top-bar control to switch the platform's display language. */
@Component({
  selector: 'app-language-switcher',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon],
  templateUrl: './language-switcher.html',
  styleUrl: './language-switcher.scss',
})
export class LanguageSwitcher {
  private readonly locale = inject(LocaleService);

  protected readonly languages = this.locale.languages;
  protected readonly current = this.locale.current;
  protected readonly open = signal(false);

  protected toggle(): void {
    this.open.update((v) => !v);
  }

  protected close(): void {
    this.open.set(false);
  }

  protected select(id: string): void {
    this.locale.setCurrent(id);
    this.close();
  }
}
