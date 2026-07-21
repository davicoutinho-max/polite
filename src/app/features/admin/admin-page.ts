import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { Select } from 'primeng/select';
import { DatePicker } from 'primeng/datepicker';
import { PartyService } from '../../core/services/party.service';
import { PlatformService } from '../../core/services/platform.service';
import { DirectoryService } from '../../core/services/directory.service';
import { SessionService } from '../../core/services/session.service';
import { ParticipationService } from '../../core/services/participation.service';
import { AlertsService } from '../../core/services/alerts.service';
import { AlertCategory, PoliticianSummary, TagSeverity } from '../../core/models';
import { digitsOnly, formatCpf, isValidCpf } from '../../shared/utils/br-documents';
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

type QuickActionKey = 'publish-content' | 'create-event' | 'send-notification' | 'create-survey';

interface QuickAction {
  readonly key: QuickActionKey;
  readonly icon: string;
  readonly label: string;
  readonly description: string;
  /** Real route for actions that navigate to an existing page; absent means it opens an inline form instead. */
  readonly route?: string;
}

type AdminTab = 'requests' | 'members' | 'politicians';

const EVENT_SEVERITY_OPTIONS: TagSeverity[] = ['secondary', 'success', 'warning', 'info', 'neutral'];
const NOTIFICATION_CATEGORY_OPTIONS: AlertCategory[] = ['party', 'campaign', 'project', 'pec', 'cpi', 'vote'];
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const HANDLE_PATTERN = /^[a-z0-9._]{3,30}$/;

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
    RouterLink,
    FormsModule,
    InputText,
    IconField,
    InputIcon,
    Select,
    DatePicker,
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
  private readonly platform = inject(PlatformService);
  protected readonly states = this.platform.states;
  protected readonly politicalPositions = this.platform.politicalPositions;
  private readonly directory = inject(DirectoryService);
  private readonly session = inject(SessionService);
  private readonly participation = inject(ParticipationService);
  private readonly alerts = inject(AlertsService);
  private readonly translate = inject(TranslateService);

  protected readonly party = this.partyService.party;
  protected readonly requests = this.partyService.requests;
  protected readonly pending = this.partyService.pendingRequests;
  protected readonly pendingCount = computed(() => this.pending().length);

  constructor() {
    const partyId = this.session.account().id;
    this.partyService.load(partyId).subscribe();
    this.partyService.reloadRequests(partyId).subscribe();
    this.partyService.reloadMembers(partyId).subscribe();
  }

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
  protected readonly registerName = signal('');
  protected readonly registerHandle = signal('');
  protected readonly registerEmail = signal('');
  protected readonly registerPassword = signal('');
  protected readonly registerCpf = signal('');
  protected readonly registerPosition = signal('');
  protected readonly registerState = signal('');
  protected readonly registerError = signal('');
  protected readonly registerSubmitting = signal(false);

  protected readonly registerNameValid = computed(() => this.registerName().trim().length > 0);
  protected readonly registerHandleValid = computed(() => {
    const handle = this.registerHandle().trim().toLowerCase();
    return !handle || HANDLE_PATTERN.test(handle);
  });
  protected readonly registerCpfValid = computed(() => isValidCpf(this.registerCpf()));
  protected readonly registerPositionValid = computed(() => this.registerPosition().trim().length > 0);
  protected readonly registerStateValid = computed(() => this.registerState().trim().length > 0);
  protected readonly registerEmailValid = computed(() => EMAIL_PATTERN.test(this.registerEmail().trim()));
  protected readonly registerPasswordValid = computed(() => this.registerPassword().length >= 8);
  protected readonly registerFormValid = computed(
    () =>
      this.registerNameValid() &&
      this.registerHandleValid() &&
      this.registerCpfValid() &&
      this.registerPositionValid() &&
      this.registerStateValid() &&
      this.registerEmailValid() &&
      this.registerPasswordValid(),
  );

  protected onCpfInput(rawValue: string): void {
    this.registerCpf.set(formatCpf(rawValue));
  }

  protected onHandleInput(rawValue: string): void {
    this.registerHandle.set(rawValue.toLowerCase().replace(/[^a-z0-9._]/g, ''));
  }

  protected readonly quickActions: (QuickAction & { labelKey: string; descKey: string })[] = [
    {
      key: 'publish-content',
      icon: 'post_add',
      label: 'Publish content',
      labelKey: 'action.publish-content',
      description: 'Share news with your base',
      descKey: 'action.publish-content.desc',
      route: '/feed',
    },
    {
      key: 'create-event',
      icon: 'event',
      label: 'Create event',
      labelKey: 'action.create-event',
      description: 'Conventions, courses, forums',
      descKey: 'action.create-event.desc',
    },
    {
      key: 'send-notification',
      icon: 'campaign',
      label: 'Send notification',
      labelKey: 'action.send-notification',
      description: 'Reach all members',
      descKey: 'action.send-notification.desc',
    },
    {
      key: 'create-survey',
      icon: 'ballot',
      label: 'Create survey',
      labelKey: 'action.create-survey',
      description: 'Collect member opinions',
      descKey: 'action.create-survey.desc',
    },
  ];

  protected onQuickAction(action: QuickAction): void {
    if (action.route) {
      return;
    }
    if (action.key === 'create-event') this.showEventForm.update((v) => !v);
    if (action.key === 'create-survey') this.showSurveyForm.update((v) => !v);
    if (action.key === 'send-notification') this.showBroadcastForm.update((v) => !v);
  }

  // ---- Create event ----
  protected readonly showEventForm = signal(false);
  protected readonly eventTitle = signal('');
  protected readonly eventDate = signal<Date | null>(null);
  protected readonly eventLocation = signal('');
  protected readonly eventTagLabel = signal('');
  protected readonly eventTagSeverity = signal<TagSeverity>('secondary');
  protected readonly eventSeverityOptions = EVENT_SEVERITY_OPTIONS;
  protected readonly eventSubmitting = signal(false);
  protected readonly eventError = signal('');

  protected submitEvent(): void {
    const title = this.eventTitle().trim();
    const date = this.eventDate();
    if (!title || !date) {
      this.eventError.set(this.translate.t('error.event-required', 'Fill in the title and date to create an event.'));
      return;
    }
    this.eventError.set('');
    this.eventSubmitting.set(true);
    const isoDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    this.partyService
      .createEvent(title, isoDate, this.eventLocation().trim(), this.eventTagLabel().trim() || 'Upcoming', this.eventTagSeverity())
      .subscribe({
        next: () => {
          this.eventSubmitting.set(false);
          this.eventTitle.set('');
          this.eventDate.set(null);
          this.eventLocation.set('');
          this.eventTagLabel.set('');
          this.showEventForm.set(false);
        },
        error: () => {
          this.eventSubmitting.set(false);
          this.eventError.set(this.translate.t('error.event-failed', 'Could not create the event.'));
        },
      });
  }

  // ---- Create survey ----
  protected readonly showSurveyForm = signal(false);
  protected readonly surveyQuestion = signal('');
  protected readonly surveyContext = signal('');
  protected readonly surveyOptions = signal<string[]>(['', '']);
  protected readonly surveySubmitting = signal(false);
  protected readonly surveyError = signal('');

  protected setSurveyOption(index: number, value: string): void {
    this.surveyOptions.update((list) => list.map((o, i) => (i === index ? value : o)));
  }

  protected addSurveyOption(): void {
    this.surveyOptions.update((list) => [...list, '']);
  }

  protected removeSurveyOption(index: number): void {
    this.surveyOptions.update((list) => (list.length > 2 ? list.filter((_, i) => i !== index) : list));
  }

  protected submitSurvey(): void {
    const question = this.surveyQuestion().trim();
    const options = this.surveyOptions().map((o) => o.trim()).filter((o) => o.length > 0);
    if (!question || options.length < 2) {
      this.surveyError.set(this.translate.t('error.survey-required', 'Fill in the question and at least two options.'));
      return;
    }
    this.surveyError.set('');
    this.surveySubmitting.set(true);
    this.participation.createSurvey(question, this.surveyContext().trim(), options).subscribe({
      next: () => {
        this.surveySubmitting.set(false);
        this.surveyQuestion.set('');
        this.surveyContext.set('');
        this.surveyOptions.set(['', '']);
        this.showSurveyForm.set(false);
      },
      error: () => {
        this.surveySubmitting.set(false);
        this.surveyError.set(this.translate.t('error.survey-failed', 'Could not create the survey.'));
      },
    });
  }

  // ---- Send notification ----
  protected readonly showBroadcastForm = signal(false);
  protected readonly broadcastTitle = signal('');
  protected readonly broadcastMessage = signal('');
  protected readonly broadcastCategory = signal<AlertCategory>('party');
  protected readonly notificationCategoryOptions = NOTIFICATION_CATEGORY_OPTIONS;
  protected readonly broadcastSubmitting = signal(false);
  protected readonly broadcastError = signal('');
  protected readonly broadcastSent = signal(false);

  protected submitBroadcast(): void {
    const title = this.broadcastTitle().trim();
    const message = this.broadcastMessage().trim();
    const recipients = this.members().map((m) => m.id);
    if (!title || !message) {
      this.broadcastError.set(this.translate.t('error.broadcast-required', 'Fill in the title and message to send a notification.'));
      return;
    }
    if (!recipients.length) {
      this.broadcastError.set(this.translate.t('error.broadcast-no-members', 'This party has no affiliated members to notify yet.'));
      return;
    }
    this.broadcastError.set('');
    this.broadcastSubmitting.set(true);
    this.alerts.broadcast(recipients, this.broadcastCategory(), 'campaign', title, message, '/wallet').subscribe({
      next: () => {
        this.broadcastSubmitting.set(false);
        this.broadcastTitle.set('');
        this.broadcastMessage.set('');
        this.broadcastSent.set(true);
        setTimeout(() => this.broadcastSent.set(false), 3000);
        this.showBroadcastForm.set(false);
      },
      error: () => {
        this.broadcastSubmitting.set(false);
        this.broadcastError.set(this.translate.t('error.broadcast-failed', 'Could not send the notification.'));
      },
    });
  }

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
    const state = this.registerState().trim();
    const email = this.registerEmail().trim();
    const password = this.registerPassword();
    const cpf = this.registerCpf();

    if (!this.registerNameValid() || !this.registerPositionValid() || !this.registerStateValid()) {
      this.registerError.set(this.translate.t('error.register-politician-required', 'Fill in the name, position and state.'));
      return;
    }
    if (!this.registerHandleValid()) {
      this.registerError.set(
        this.translate.t('error.invalid-handle', 'Handle can only contain lowercase letters, numbers, dots and underscores (3–30 characters).'),
      );
      return;
    }
    if (!this.registerCpfValid()) {
      this.registerError.set(this.translate.t('error.invalid-cpf', 'Enter a valid CPF.'));
      return;
    }
    if (!this.registerEmailValid()) {
      this.registerError.set(this.translate.t('error.invalid-email', 'Enter a valid email address.'));
      return;
    }
    if (!this.registerPasswordValid()) {
      this.registerError.set(this.translate.t('error.invalid-password', 'Password must be at least 8 characters.'));
      return;
    }

    this.registerError.set('');
    this.registerSubmitting.set(true);
    const handle = this.registerHandle().trim() || `${name.toLowerCase().replace(/[^a-z0-9]+/g, '')}${Math.floor(Math.random() * 10000)}`;
    this.partyService
      .registerPolitician(this.party().id, { name, handle, email, password, documentNumber: digitsOnly(cpf), roleTitle: position, state })
      .subscribe({
        next: () => {
          this.registerSubmitting.set(false);
          this.directory.reloadPoliticians().subscribe();
          this.resetRegisterForm();
        },
        error: () => {
          this.registerSubmitting.set(false);
          this.registerError.set(this.translate.t('error.register-politician-failed', 'Could not register this politician. That email or CPF may already be in use.'));
        },
      });
  }

  private resetRegisterForm(): void {
    this.registerName.set('');
    this.registerHandle.set('');
    this.registerEmail.set('');
    this.registerPassword.set('');
    this.registerCpf.set('');
    this.registerPosition.set('');
    this.registerState.set('');
    this.registerError.set('');
  }
}
