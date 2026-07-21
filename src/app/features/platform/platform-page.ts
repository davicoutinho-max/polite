import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { PlatformService } from '../../core/services/platform.service';
import { TranslateService } from '../../core/services/translate.service';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiSection } from '../../shared/ui/ui-section/ui-section';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { UiAvatar } from '../../shared/ui/ui-avatar/ui-avatar';
import { UiTag } from '../../shared/ui/ui-tag/ui-tag';
import { UiEmpty } from '../../shared/ui/ui-empty/ui-empty';
import { UiTabs, UiTab } from '../../shared/ui/ui-tabs/ui-tabs';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

type PlatformTab = 'directory' | 'regions' | 'positions' | 'languages';
type SelectOption = { value: string; label: string };

/** Platform administration: party registry, politician assignment and platform-wide parameters. */
@Component({
  selector: 'app-platform-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeader, UiSection, UiIcon, UiButton, UiAvatar, UiTag, UiEmpty, UiTabs, FormsModule, InputText, Select, TranslatePipe],
  templateUrl: './platform-page.html',
  styleUrl: './platform-page.scss',
})
export class PlatformPage {
  private readonly platform = inject(PlatformService);
  private readonly translate = inject(TranslateService);

  protected readonly tabs: UiTab[] = [
    { id: 'directory', label: 'Parties & Politicians', key: 'tab.parties-politicians', icon: 'how_to_reg' },
    { id: 'regions', label: 'Countries & States', key: 'tab.countries-states', icon: 'public' },
    { id: 'positions', label: 'Political Positions', key: 'tab.political-positions', icon: 'badge' },
    { id: 'languages', label: 'Languages', key: 'tab.languages', icon: 'translate' },
  ];
  protected readonly activeTab = signal<PlatformTab>('directory');

  protected setActiveTab(id: string): void {
    this.activeTab.set(id as PlatformTab);
  }

  protected readonly parties = this.platform.parties;
  protected readonly politicians = this.platform.politicians;

  protected readonly partyAssignOptions = computed<SelectOption[]>(() => [
    { value: '', label: this.translate.t('label.independent', 'Independent') },
    ...this.parties().map((p) => ({ value: p.id, label: p.name })),
  ]);

  protected readonly avatarPlaceholder =
    "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 40 40'%3E%3Crect width='40' height='40' fill='%23c7ccd1'/%3E%3Ccircle cx='20' cy='15' r='7' fill='%23fff'/%3E%3Cpath d='M6 38c0-8 6-13 14-13s14 5 14 13z' fill='%23fff'/%3E%3C/svg%3E";

  // ---- New party form ----
  protected readonly showForm = signal(false);
  protected readonly name = signal('');
  protected readonly acronym = signal('');
  protected readonly number = signal<number | null>(null);
  protected readonly president = signal('');
  protected readonly ideology = signal('');
  protected readonly handle = signal('');
  protected readonly email = signal('');
  protected readonly password = signal('');
  protected readonly documentNumber = signal('');
  protected readonly createSubmitting = signal(false);
  protected readonly createError = signal('');

  protected partyName(id: string | null): string {
    return this.platform.partyName(id);
  }

  protected toggleForm(): void {
    this.showForm.update((v) => !v);
  }

  protected createParty(): void {
    const number = this.number();
    if (
      !this.name().trim() ||
      !this.acronym().trim() ||
      !number ||
      !this.handle().trim() ||
      !this.email().trim() ||
      !this.password().trim() ||
      !this.documentNumber().trim()
    ) {
      return;
    }
    this.createSubmitting.set(true);
    this.createError.set('');
    this.platform
      .createParty({
        name: this.name().trim(),
        acronym: this.acronym().trim().toUpperCase(),
        number,
        president: this.president().trim() || '—',
        ideology: this.ideology().trim() || '—',
        handle: this.handle().trim(),
        email: this.email().trim(),
        password: this.password(),
        documentNumber: this.documentNumber().trim(),
      })
      .subscribe({
        next: () => {
          this.createSubmitting.set(false);
          this.resetForm();
        },
        error: () => {
          this.createSubmitting.set(false);
          this.createError.set('Could not create the party. Check the fields and try again.');
        },
      });
  }

  protected assign(politicianId: string, value: string): void {
    this.platform.assignPolitician(politicianId, value === '' || value == null ? null : value).subscribe();
  }

  private resetForm(): void {
    this.name.set('');
    this.acronym.set('');
    this.number.set(null);
    this.president.set('');
    this.ideology.set('');
    this.handle.set('');
    this.email.set('');
    this.password.set('');
    this.documentNumber.set('');
    this.showForm.set(false);
  }

  // ---- Countries & states ----
  protected readonly countries = this.platform.countries;
  protected readonly states = this.platform.states;

  protected readonly countryName = signal('');
  protected readonly countryCode = signal('');
  protected readonly stateName = signal('');
  protected readonly stateCode = signal('');
  protected readonly stateCountryId = signal('');

  protected readonly countryOptions = computed<SelectOption[]>(() =>
    this.countries().map((c) => ({ value: c.id, label: c.name })),
  );

  protected countryNameOf(id: string): string {
    return this.platform.countryName(id);
  }

  protected addCountry(): void {
    if (!this.countryName().trim() || !this.countryCode().trim()) return;
    this.platform.addCountry(this.countryName().trim(), this.countryCode().trim());
    this.countryName.set('');
    this.countryCode.set('');
  }

  protected removeCountry(id: string): void {
    this.platform.removeCountry(id);
  }

  protected addState(): void {
    const countryId = this.stateCountryId() || this.countries()[0]?.id;
    if (!this.stateName().trim() || !this.stateCode().trim() || !countryId) return;
    this.platform.addState(this.stateName().trim(), this.stateCode().trim(), countryId);
    this.stateName.set('');
    this.stateCode.set('');
  }

  protected removeState(id: string): void {
    this.platform.removeState(id);
  }

  // ---- Political positions (cargos) ----
  protected readonly politicalPositions = this.platform.politicalPositions;
  protected readonly positionName = signal('');

  protected addPosition(): void {
    if (!this.positionName().trim()) return;
    this.platform.addPoliticalPosition(this.positionName().trim());
    this.positionName.set('');
  }

  protected removePosition(id: string): void {
    this.platform.removePoliticalPosition(id);
  }

  // ---- Languages ----
  protected readonly languages = this.platform.languages;
  protected readonly languageName = signal('');
  protected readonly languageCode = signal('');

  protected addLanguage(): void {
    if (!this.languageName().trim() || !this.languageCode().trim()) return;
    this.platform.addLanguage(this.languageName().trim(), this.languageCode().trim());
    this.languageName.set('');
    this.languageCode.set('');
  }

  protected removeLanguage(id: string): void {
    this.platform.removeLanguage(id);
  }

  protected setDefaultLanguage(id: string): void {
    this.platform.setDefaultLanguage(id);
  }

  // ---- Translation tags ----
  protected readonly translations = this.platform.translations;
  protected readonly newTranslationKey = signal('');
  protected readonly newTranslationValue = signal('');

  protected addTranslation(): void {
    if (!this.newTranslationKey().trim() || !this.newTranslationValue().trim()) return;
    this.platform.addTranslation(this.newTranslationKey().trim(), this.newTranslationValue().trim());
    this.newTranslationKey.set('');
    this.newTranslationValue.set('');
  }

  protected updateTranslationValue(id: string, languageId: string, value: string): void {
    this.platform.updateTranslationValue(id, languageId, value);
  }

  protected removeTranslation(id: string): void {
    this.platform.removeTranslation(id);
  }
}
