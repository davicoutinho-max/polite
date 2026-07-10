import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { PrivacyService } from '../../core/services/privacy.service';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiSection } from '../../shared/ui/ui-section/ui-section';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** LGPD privacy & data control centre: consent, portability and erasure. */
@Component({
  selector: 'app-privacy-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeader, UiSection, UiIcon, UiButton, TranslatePipe],
  templateUrl: './privacy-page.html',
  styleUrl: './privacy-page.scss',
})
export class PrivacyPage {
  private readonly privacy = inject(PrivacyService);

  protected readonly consents = this.privacy.consents;
  protected readonly rights = this.privacy.rights;
  protected readonly confirmingDelete = signal(false);

  protected toggle(id: string): void {
    this.privacy.toggleConsent(id);
  }

  protected exportData(): void {
    this.privacy.exportData();
  }

  protected requestDelete(): void {
    this.confirmingDelete.set(true);
  }

  protected cancelDelete(): void {
    this.confirmingDelete.set(false);
  }

  protected confirmDelete(): void {
    this.privacy.deleteAccount();
    this.confirmingDelete.set(false);
  }
}
