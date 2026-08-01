import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Checkbox } from 'primeng/checkbox';
import { DatePicker } from 'primeng/datepicker';
import { InputOtp } from 'primeng/inputotp';
import { InputText } from 'primeng/inputtext';
import { Observable } from 'rxjs';
import { LegislativeBillSummary, Petition, StartPetitionSignatureCommand } from '../../../../core/models';
import { AiAssistantService } from '../../../../core/services/ai-assistant.service';
import { LegislativeOpenDataService } from '../../../../core/services/legislative-open-data.service';
import { ParticipationService } from '../../../../core/services/participation.service';
import { SessionService } from '../../../../core/services/session.service';
import { TranslateService } from '../../../../core/services/translate.service';
import { formatCpf } from '../../../../shared/utils/br-documents';
import { CompactNumberPipe } from '../../../../shared/pipes/compact-number.pipe';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { AskAi, AskAiPromptOption } from '../../../../shared/ai/ask-ai/ask-ai';
import { BillCard } from '../../../../shared/legislative/bill-card/bill-card';
import { UiButton } from '../../../../shared/ui/ui-button/ui-button';
import { UiCard } from '../../../../shared/ui/ui-card/ui-card';
import { UiDialog } from '../../../../shared/ui/ui-dialog/ui-dialog';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { UiProgress } from '../../../../shared/ui/ui-progress/ui-progress';
import { UiTag } from '../../../../shared/ui/ui-tag/ui-tag';

type SignStep = 'closed' | 'confirm' | 'form' | 'code' | 'done';

/** Order-tracking-style stepper shown at the top of the sign wizard — mirrors the visual language
 * used for the affiliation and bill-history timelines (done steps green, current step badged). */
const SIGN_WIZARD_STEPS: ReadonlyArray<{
  readonly step: Exclude<SignStep, 'closed' | 'done'>;
  readonly icon: string;
  readonly labelKey: string;
  readonly labelDefault: string;
}> = [
  { step: 'confirm', icon: 'fact_check', labelKey: 'label.sign-step-confirm', labelDefault: 'Confirm' },
  { step: 'form', icon: 'edit_document', labelKey: 'label.sign-step-data', labelDefault: 'Your data' },
  { step: 'code', icon: 'mail', labelKey: 'label.sign-step-verify', labelDefault: 'Verification' },
];

/** Matches LegislativeOpenDataService's own page size — used here only to guess whether a "load
 * more" click is likely to reveal anything. */
const BILLS_PAGE_SIZE = 8;

/** Petition (abaixo-assinado) card with signature progress, attachments, a "View details" dialog,
 * a "Related bills" dialog searching real bills related to this petition straight from the
 * federal legislature's own open-data APIs (Câmara dos Deputados / Senado Federal — see
 * LegislativeOpenDataService and BillCard, not internal or LLM-generated data), and a
 * DocuSign-like sign wizard: confirm → tiered identity capture → code verification. */
@Component({
  selector: 'app-petition-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    InputText,
    DatePicker,
    Checkbox,
    InputOtp,
    UiCard,
    UiTag,
    UiButton,
    UiProgress,
    UiIcon,
    UiDialog,
    BillCard,
    AskAi,
    CompactNumberPipe,
    TranslatePipe,
  ],
  templateUrl: './petition-card.html',
  styleUrl: './petition-card.scss',
})
export class PetitionCard {
  private readonly participation = inject(ParticipationService);
  private readonly session = inject(SessionService);
  private readonly translate = inject(TranslateService);
  private readonly legislativeOpenData = inject(LegislativeOpenDataService);
  private readonly aiAssistant = inject(AiAssistantService);

  readonly petition = input.required<Petition>();

  protected readonly percent = computed(() => Math.min(100, Math.round((this.petition().signatures / this.petition().goal) * 100)));

  protected readonly isPopularInitiative = computed(() => this.petition().petitionType === 'popular_initiative');

  protected readonly askAiFn = computed(
    () => (question: string): Observable<string> =>
      this.aiAssistant.askAboutParticipationItem('petition', this.petition().title, this.petition().summary, question),
  );
  protected readonly askAiPrompts: AskAiPromptOption[] = [
    {
      question: 'Summarize this petition in one or two sentences.',
      label: this.translate.t('button.ask-ai-summarize', 'Summarize'),
      icon: 'summarize',
    },
    {
      question: 'Who would this petition affect if it succeeds?',
      label: this.translate.t('button.ask-ai-who-affected', 'Who is affected?'),
      icon: 'groups',
    },
    {
      question: 'What happens after this petition reaches its signature goal?',
      label: this.translate.t('button.ask-ai-whats-next', "What's next?"),
      icon: 'arrow_forward',
    },
  ];

  // ---- View details ----
  protected readonly showDetails = signal(false);

  // ---- Related bills (real bills from Câmara/Senado open-data APIs) ----
  protected readonly showBills = signal(false);
  protected readonly billsLoading = signal(false);
  protected readonly billsError = signal('');
  protected readonly bills = signal<LegislativeBillSummary[]>([]);
  protected readonly billsLoadingMore = signal(false);
  protected readonly billsExhausted = signal(false);
  private billsSearched = false;
  private billsPage = 1;

  // ---- Sign wizard ----
  protected readonly signWizardSteps = SIGN_WIZARD_STEPS;
  protected readonly signStep = signal<SignStep>('closed');
  protected readonly signError = signal('');
  protected readonly signSubmitting = signal(false);

  protected readonly fullName = signal('');
  protected readonly cpf = signal('');
  protected readonly birthDate = signal<Date | null>(null);
  protected readonly city = signal('');
  protected readonly state = signal('');
  protected readonly contact = signal('');
  protected readonly electoralData = signal('');
  protected readonly eSignatureConsent = signal(false);
  protected readonly typedSignature = signal('');

  protected readonly verificationId = signal<string | null>(null);
  protected readonly codeInput = signal('');

  /** True once the account's real, registered name/CPF/email have loaded — the corresponding
   * form fields become read-only at that point so a signer can never type a name or CPF other
   * than the one already on file with their account (this is a legal signature, not free text). */
  protected readonly identityLocked = signal(false);
  /** The account's email is always known (no extra fetch needed) — locked the same way as
   * name/CPF so a signer can't redirect their verification code to an inbox that isn't theirs. */
  protected readonly emailLocked = computed(() => !!this.session.account().email);

  protected openDetails(): void {
    this.showDetails.set(true);
  }

  protected openBills(): void {
    this.showBills.set(true);
    if (this.billsSearched) {
      return;
    }
    this.billsSearched = true;
    this.billsLoading.set(true);
    this.billsError.set('');
    const keyword = this.petition().category || this.petition().title;
    this.legislativeOpenData.searchBills(keyword, 1).subscribe({
      next: (bills) => {
        this.billsLoading.set(false);
        this.bills.set(bills);
        this.billsExhausted.set(bills.length < BILLS_PAGE_SIZE);
        if (bills.length === 0) {
          this.billsError.set(this.translate.t('error.bills-none-found', 'No related bills were found on Câmara/Senado for this topic.'));
        }
      },
      error: () => {
        this.billsLoading.set(false);
        this.billsError.set(this.translate.t('error.bills-search-failed', 'Could not reach Câmara/Senado open-data services right now.'));
      },
    });
  }

  protected loadMoreBills(): void {
    const keyword = this.petition().category || this.petition().title;
    const nextPage = this.billsPage + 1;
    this.billsLoadingMore.set(true);
    this.legislativeOpenData.searchBills(keyword, nextPage).subscribe({
      next: (bills) => {
        this.billsLoadingMore.set(false);
        const previousCount = this.bills().length;
        this.billsPage = nextPage;
        this.bills.set(bills);
        if (bills.length <= previousCount) {
          this.billsExhausted.set(true);
        }
      },
      error: () => {
        this.billsLoadingMore.set(false);
        this.billsExhausted.set(true);
      },
    });
  }

  protected signWizardStepState(step: Exclude<SignStep, 'closed' | 'done'>): 'done' | 'active' | 'pending' {
    const order: Exclude<SignStep, 'closed' | 'done'>[] = ['confirm', 'form', 'code'];
    const current = this.signStep();
    if (current === 'closed' || current === 'done') {
      return 'done';
    }
    const currentIndex = order.indexOf(current);
    const stepIndex = order.indexOf(step);
    if (stepIndex < currentIndex) {
      return 'done';
    }
    return stepIndex === currentIndex ? 'active' : 'pending';
  }

  protected startSignWizard(): void {
    const account = this.session.account();
    this.fullName.set(account.name ?? '');
    this.cpf.set('');
    this.birthDate.set(null);
    this.city.set('');
    this.state.set('');
    this.contact.set(account.email ?? '');
    this.electoralData.set('');
    this.eSignatureConsent.set(false);
    this.typedSignature.set('');
    this.signError.set('');
    this.identityLocked.set(false);
    this.signStep.set('confirm');

    // Best-effort: prefills and locks name/CPF against the account's real registered document so
    // a signer can't type a different one. Falls back to a free-text CPF field (previous
    // behavior) if this account genuinely has none on file rather than blocking signing outright.
    this.session.getMyDocumentProfile().subscribe({
      next: (profile) => {
        this.fullName.set(profile.name);
        this.cpf.set(formatCpf(profile.documentNumber));
        this.identityLocked.set(true);
      },
      error: () => undefined,
    });
  }

  protected closeSignWizard(): void {
    this.signStep.set('closed');
    this.signError.set('');
    this.signSubmitting.set(false);
  }

  protected confirmWantsToSign(): void {
    this.signStep.set('form');
  }

  protected submitSignatureForm(): void {
    if (!this.fullName().trim() || !this.cpf().trim() || !this.typedSignature().trim() || !this.eSignatureConsent()) {
      this.signError.set(
        this.translate.t('error.petition-sign-required', 'Fill in your name, CPF, consent and typed signature to continue.'),
      );
      return;
    }
    if (!this.contact().trim()) {
      this.signError.set(this.translate.t('error.petition-sign-email-required', 'Enter the email address to receive your verification code.'));
      return;
    }
    this.signError.set('');
    this.signSubmitting.set(true);

    const birthDate = this.birthDate();
    const isoBirthDate = birthDate
      ? `${birthDate.getFullYear()}-${String(birthDate.getMonth() + 1).padStart(2, '0')}-${String(birthDate.getDate()).padStart(2, '0')}`
      : null;

    const command: StartPetitionSignatureCommand = {
      fullName: this.fullName().trim(),
      cpf: this.cpf().trim(),
      birthDate: isoBirthDate,
      city: this.city().trim() || null,
      state: this.state().trim() || null,
      verificationMethod: 'email',
      contact: this.contact().trim() || null,
      electoralData: this.isPopularInitiative() ? this.electoralData().trim() || null : null,
      eSignatureConsent: this.eSignatureConsent(),
      typedSignature: this.typedSignature().trim(),
    };

    this.participation.startPetitionSignature(this.petition().id, command).subscribe({
      next: (started) => {
        this.signSubmitting.set(false);
        this.verificationId.set(started.verificationId);
        this.codeInput.set('');
        this.signStep.set('code');
      },
      error: () => {
        this.signSubmitting.set(false);
        this.signError.set(
          this.translate.t(
            'error.petition-sign-start-failed',
            'Could not start the signature — check your CPF/email, or the verification email service may be temporarily unavailable.',
          ),
        );
      },
    });
  }

  protected submitCode(): void {
    const verificationId = this.verificationId();
    if (!verificationId || !this.codeInput().trim()) {
      return;
    }
    this.signError.set('');
    this.signSubmitting.set(true);
    this.participation.confirmPetitionSignature(this.petition().id, verificationId, this.codeInput().trim()).subscribe({
      next: () => {
        this.signSubmitting.set(false);
        this.signStep.set('done');
      },
      error: () => {
        this.signSubmitting.set(false);
        this.signError.set(this.translate.t('error.petition-sign-code-invalid', 'Incorrect or expired code — please try again.'));
      },
    });
  }
}
