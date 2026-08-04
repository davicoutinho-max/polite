import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { Select } from 'primeng/select';
import { PartyInvite, PlatformService } from '../../core/services/platform.service';
import { DirectoryService } from '../../core/services/directory.service';
import { AlertsService } from '../../core/services/alerts.service';
import { TranslateService } from '../../core/services/translate.service';
import { InfiniteScrollDirective } from '../../core/directives/infinite-scroll.directive';
import { PartyRegistryEntry } from '../../core/models';
import { digitsOnly, formatCnpj, isValidCnpj } from '../../shared/utils/br-documents';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiSection } from '../../shared/ui/ui-section/ui-section';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { UiAvatar } from '../../shared/ui/ui-avatar/ui-avatar';
import { UiTag } from '../../shared/ui/ui-tag/ui-tag';
import { UiEmpty } from '../../shared/ui/ui-empty/ui-empty';
import { UiTabs, UiTab } from '../../shared/ui/ui-tabs/ui-tabs';
import { CompactNumberPipe } from '../../shared/pipes/compact-number.pipe';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

type PlatformTab = 'directory' | 'regions' | 'positions' | 'languages';
type SelectOption = { value: string; label: string };
type PartySortKey = 'members' | 'name';

const PARTY_PAGE_SIZE = 9;

/** Platform administration: party registry, politician assignment and platform-wide parameters. */
@Component({
  selector: 'app-platform-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    PageHeader,
    UiSection,
    UiIcon,
    UiButton,
    UiAvatar,
    UiTag,
    UiEmpty,
    UiTabs,
    FormsModule,
    InputText,
    IconField,
    InputIcon,
    Select,
    InfiniteScrollDirective,
    CompactNumberPipe,
    TranslatePipe,
  ],
  templateUrl: './platform-page.html',
  styleUrl: './platform-page.scss',
})
export class PlatformPage {
  private readonly platform = inject(PlatformService);
  private readonly directory = inject(DirectoryService);
  private readonly translate = inject(TranslateService);
  private readonly alerts = inject(AlertsService);

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

  // ---- Registered-parties search/sort/infinite-scroll — same pattern as the public parties
  // directory (parties-page.ts), since this registry has no upper bound either (every real party
  // plus anything ever registered through it) and rendering it all at once doesn't scale. ----
  protected readonly partySearch = signal('');
  protected readonly partySort = signal<PartySortKey>('members');
  protected readonly partyVisibleCount = signal(PARTY_PAGE_SIZE);

  protected readonly partySortOptions: SelectOption[] = [
    { value: 'members', label: this.translate.t('label.largest', 'Largest') },
    { value: 'name', label: this.translate.t('label.name-az', 'Name (A–Z)') },
  ];

  protected readonly filteredParties = computed<PartyRegistryEntry[]>(() => {
    const q = this.partySearch().trim().toLowerCase();
    const list = this.parties().filter((p) => {
      if (!q) return true;
      return p.name.toLowerCase().includes(q) || p.acronym.toLowerCase().includes(q) || p.ideology.toLowerCase().includes(q);
    });
    const sort = this.partySort();
    return [...list].sort((a, b) => (sort === 'name' ? a.name.localeCompare(b.name) : b.memberCount - a.memberCount));
  });

  protected readonly partyResultCount = computed(() => this.filteredParties().length);
  protected readonly visibleParties = computed(() => this.filteredParties().slice(0, this.partyVisibleCount()));
  protected readonly allPartiesLoaded = computed(() => this.partyVisibleCount() >= this.partyResultCount());

  protected setPartySearch(value: string): void {
    this.partySearch.set(value);
  }

  protected loadMoreParties(): void {
    if (!this.allPartiesLoaded()) {
      this.partyVisibleCount.update((c) => c + PARTY_PAGE_SIZE);
    }
  }

  protected partyLogo(id: string): string | null {
    return this.directory.parties().find((p) => p.id === id)?.logoUrl ?? null;
  }

  // ---- Party invite form (replaces the old "admin sets the party's password directly" flow —
  // the party's own contact now redeems the token and picks their own password, see
  // ManagePartyInviteUseCase's javadoc on the backend) ----
  protected readonly showForm = signal(false);
  protected readonly name = signal('');
  protected readonly acronym = signal('');
  protected readonly number = signal<number | null>(null);
  protected readonly president = signal('');
  protected readonly ideology = signal('');
  protected readonly cnpj = signal('');
  protected readonly targetEmail = signal('');
  protected readonly createSubmitting = signal(false);
  protected readonly createError = signal('');

  protected onCnpjInput(raw: string): void {
    this.cnpj.set(formatCnpj(raw));
  }

  protected readonly invites = signal<PartyInvite[]>([]);
  protected readonly resendingInviteId = signal<string | null>(null);

  constructor() {
    this.reloadInvites();
    const partyFilterKey = computed(() => `${this.partySearch()}|${this.partySort()}`);
    effect(() => {
      partyFilterKey();
      this.partyVisibleCount.set(PARTY_PAGE_SIZE);
    });
  }

  private reloadInvites(): void {
    this.platform.listPartyInvites().subscribe((list) => this.invites.set(list));
  }

  protected partyName(id: string | null): string {
    return this.platform.partyName(id);
  }

  protected toggleForm(): void {
    this.showForm.update((v) => !v);
  }

  protected createParty(): void {
    const number = this.number();
    if (!this.name().trim() || !this.acronym().trim() || !number || !this.cnpj().trim() || !this.targetEmail().trim()) {
      return;
    }
    if (!isValidCnpj(this.cnpj())) {
      this.createError.set(this.translate.t('error.invalid-cnpj', 'Enter a valid CNPJ.'));
      return;
    }
    this.createSubmitting.set(true);
    this.createError.set('');
    this.platform
      .issuePartyInvite({
        name: this.name().trim(),
        acronym: this.acronym().trim().toUpperCase(),
        number,
        president: this.president().trim() || '—',
        ideology: this.ideology().trim() || '—',
        cnpj: digitsOnly(this.cnpj()),
        targetEmail: this.targetEmail().trim(),
      })
      .subscribe({
        next: (invite) => {
          this.createSubmitting.set(false);
          this.invites.update((list) => [invite, ...list]);
          this.resetForm();
          this.notifySuccess(this.translate.t('title.invite-sent', 'Invite sent'), invite.targetEmail ?? '');
        },
        error: () => {
          this.createSubmitting.set(false);
          this.createError.set('Could not create the invite. Check the fields and try again.');
        },
      });
  }

  protected resendInvite(id: string): void {
    this.resendingInviteId.set(id);
    this.platform.resendPartyInvite(id).subscribe({
      next: () => {
        this.resendingInviteId.set(null);
        this.reloadInvites();
        this.notifySuccess(this.translate.t('title.invite-resent', 'Invite resent'), '');
      },
      error: () => this.resendingInviteId.set(null),
    });
  }

  protected assign(politicianId: string, value: string): void {
    this.platform.assignPolitician(politicianId, value === '' || value == null ? null : value).subscribe({
      next: () => this.notifySuccess(this.translate.t('title.assignment-updated', 'Assignment updated'), ''),
    });
  }

  private notifySuccess(title: string, message: string): void {
    this.alerts.push({ category: 'project', icon: 'check_circle', title, message, timeLabel: this.translate.t('label.just-now', 'Just now') });
  }

  private resetForm(): void {
    this.name.set('');
    this.acronym.set('');
    this.number.set(null);
    this.president.set('');
    this.ideology.set('');
    this.cnpj.set('');
    this.targetEmail.set('');
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
