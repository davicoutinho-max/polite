import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { PartyService } from '../../core/services/party.service';
import { DirectoryService, LEVEL_OPTIONS } from '../../core/services/directory.service';
import { GovLevel, PoliticianSummary, TagSeverity } from '../../core/models';
import { isValidCnpj, isValidCpf } from '../../shared/utils/br-documents';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiSection } from '../../shared/ui/ui-section/ui-section';
import { UiStat } from '../../shared/ui/ui-stat/ui-stat';
import { UiTag } from '../../shared/ui/ui-tag/ui-tag';
import { UiAvatar } from '../../shared/ui/ui-avatar/ui-avatar';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiEmpty } from '../../shared/ui/ui-empty/ui-empty';
import { UiTabs, UiTab } from '../../shared/ui/ui-tabs/ui-tabs';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { TranslateService } from '../../core/services/translate.service';

interface QuickAction {
  readonly icon: string;
  readonly label: string;
  readonly description: string;
}

type AdminTab = 'requests' | 'members' | 'politicians';

const REQUEST_SEVERITY: Record<string, TagSeverity> = {
  pending: 'warning',
  approved: 'success',
  rejected: 'danger',
};

/** Party administration panel. */
@Component({
  selector: 'app-admin-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    InputText,
    IconField,
    InputIcon,
    PageHeader,
    UiSection,
    UiStat,
    UiTag,
    UiAvatar,
    UiButton,
    UiIcon,
    UiEmpty,
    UiTabs,
    TranslatePipe,
  ],
  templateUrl: './admin-page.html',
  styleUrl: './admin-page.scss',
})
export class AdminPage {
  private readonly partyService = inject(PartyService);
  private readonly directory = inject(DirectoryService);
  private readonly translate = inject(TranslateService);

  protected readonly party = this.partyService.party;
  protected readonly requests = this.partyService.requests;
  protected readonly pending = this.partyService.pendingRequests;
  protected readonly pendingCount = computed(() => this.pending().length);

  protected readonly tabs: UiTab[] = [
    { id: 'requests', label: 'Requests', key: 'tab.requests', icon: 'how_to_reg' },
    { id: 'members', label: 'Members', key: 'tab.members', icon: 'groups' },
    { id: 'politicians', label: 'Politicians', key: 'tab.politicians', icon: 'badge' },
  ];
  protected readonly activeTab = signal<AdminTab>('requests');

  // ---- Members ----
  protected readonly members = this.partyService.members;
  protected readonly memberSearch = signal('');
  protected readonly filteredMembers = computed(() => {
    const q = this.memberSearch().trim().toLowerCase();
    if (!q) return this.members();
    return this.members().filter((m) => m.name.toLowerCase().includes(q) || m.city.toLowerCase().includes(q));
  });

  // ---- Linked politicians ----
  protected readonly candidateRepresentatives = computed<PoliticianSummary[]>(() => {
    const linkedIds = new Set(this.party().representatives.map((r) => r.id));
    return this.directory.politicians().filter((p) => p.partyId === this.party().id && !linkedIds.has(p.id));
  });

  // ---- Register a new politician ----
  protected readonly levelOptions = LEVEL_OPTIONS;
  protected readonly registerName = signal('');
  protected readonly registerHandle = signal('');
  protected readonly registerCpf = signal('');
  protected readonly registerCnpj = signal('');
  protected readonly registerPosition = signal('');
  protected readonly registerLevel = signal<GovLevel>('federal');
  protected readonly registerState = signal('');
  protected readonly registerError = signal('');

  protected readonly quickActions: (QuickAction & { labelKey: string; descKey: string })[] = [
    { icon: 'post_add', label: 'Publish content', labelKey: 'action.publish-content', description: 'Share news with your base', descKey: 'action.publish-content.desc' },
    { icon: 'event', label: 'Create event', labelKey: 'action.create-event', description: 'Conventions, courses, forums', descKey: 'action.create-event.desc' },
    { icon: 'campaign', label: 'Send notification', labelKey: 'action.send-notification', description: 'Reach all members', descKey: 'action.send-notification.desc' },
    { icon: 'ballot', label: 'Create survey', labelKey: 'action.create-survey', description: 'Collect member opinions', descKey: 'action.create-survey.desc' },
  ];

  protected requestSeverity(status: string): TagSeverity {
    return REQUEST_SEVERITY[status] ?? 'neutral';
  }

  protected requestStatusLabel(status: string): string {
    return this.translate.t(`status.${status}`, status);
  }

  protected onApprove(id: string): void {
    this.partyService.approveRequest(id);
  }

  protected onReject(id: string): void {
    this.partyService.rejectRequest(id);
  }

  protected setActiveTab(id: string): void {
    this.activeTab.set(id as AdminTab);
  }

  protected setMemberSearch(value: string): void {
    this.memberSearch.set(value);
  }

  protected onToggleMember(id: string): void {
    this.partyService.toggleMemberStatus(id);
  }

  protected onAddRepresentative(candidate: PoliticianSummary): void {
    this.partyService.addRepresentative(candidate);
  }

  protected onRemoveRepresentative(id: string): void {
    this.partyService.removeRepresentative(id);
  }

  protected submitNewPolitician(): void {
    const name = this.registerName().trim();
    const position = this.registerPosition().trim();
    const state = this.registerState().trim().toUpperCase();
    const cpf = this.registerCpf();
    const cnpj = this.registerCnpj();

    if (!name || !position || !state) {
      this.registerError.set(this.translate.t('error.register-politician-required', 'Fill in the name, position and state to register a politician.'));
      return;
    }
    if (!isValidCpf(cpf)) {
      this.registerError.set(this.translate.t('error.invalid-cpf', 'Enter a valid CPF (11 digits).'));
      return;
    }
    if (!isValidCnpj(cnpj)) {
      this.registerError.set(this.translate.t('error.invalid-cnpj', 'Enter a valid CNPJ (14 digits).'));
      return;
    }

    this.registerError.set('');
    const handle = this.registerHandle().trim() || `@${name.toLowerCase().replace(/[^a-z0-9]+/g, '.')}`;
    const pol = this.directory.addPolitician({
      name,
      handle,
      cpf,
      cnpj,
      position,
      level: this.registerLevel(),
      state,
      partyId: this.party().id,
      partyAcronym: this.party().acronym,
    });
    this.partyService.addRepresentative(pol);
    this.resetRegisterForm();
  }

  private resetRegisterForm(): void {
    this.registerName.set('');
    this.registerHandle.set('');
    this.registerCpf.set('');
    this.registerCnpj.set('');
    this.registerPosition.set('');
    this.registerLevel.set('federal');
    this.registerState.set('');
    this.registerError.set('');
  }
}
