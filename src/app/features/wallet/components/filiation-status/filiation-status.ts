import { ChangeDetectionStrategy, Component, ElementRef, computed, effect, inject, input, output, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { FiliationStatus, FiliationStep, PartySummary } from '../../../../core/models';
import { MediaService } from '../../../../core/services/media.service';
import { PlatformService } from '../../../../core/services/platform.service';
import { SessionService } from '../../../../core/services/session.service';
import { UiButton } from '../../../../shared/ui/ui-button/ui-button';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { UiSection } from '../../../../shared/ui/ui-section/ui-section';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

export interface AffiliationRequestPayload {
  readonly partyId: string;
  readonly city: string;
  /** Título de eleitor and the rest of the voter-registration/identity data TSE Resolução
   * 23.571/2018 requires for a valid affiliation request — see the wizard steps below. */
  readonly voterRegistrationNumber: string;
  readonly electoralZone: string;
  readonly electoralSection: string;
  readonly electoralState: string;
  readonly electoralMunicipality: string;
  readonly identityPhotoUrl: string;
}

/** Pre-submission data-collection wizard steps — distinct from the post-submission lifecycle
 * (`steps`/`currentIndex` inputs below), which only starts once the real request above has
 * actually been sent. */
type WizardStep = 'data' | 'identity';

/**
 * Official affiliation flow visualiser. The confirmation genuinely depends on
 * the party and the Electoral Justice — the app only orchestrates the request.
 *
 * Before that request can even be sent, TSE Resolução 23.571/2018 requires the citizen's
 * voter-registration data (título de eleitor, zona, seção, UF) and a photo identity check for
 * the affiliation's electronic signature to be valid — collected here as a two-step wizard
 * ("Confirme seus dados" → "Confirme sua identidade") before `request` is ever emitted.
 */
@Component({
  selector: 'app-filiation-status',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, InputText, Select, UiSection, UiIcon, UiButton, TranslatePipe],
  templateUrl: './filiation-status.html',
  styleUrl: './filiation-status.scss',
})
export class FiliationStatusComponent {
  private readonly session = inject(SessionService);
  private readonly platform = inject(PlatformService);
  private readonly media = inject(MediaService);

  readonly steps = input.required<readonly FiliationStep[]>();
  readonly status = input.required<FiliationStatus>();
  /** Index of the current (last completed) step; -1 when not started. */
  readonly currentIndex = input.required<number>();
  /** Real per-stage timestamps from the audit trail (membership-affiliation-service's
   * AffiliationStatusHistoryEntry) — shown next to done/active steps when available. */
  readonly stepDates = input<ReadonlyMap<FiliationStatus, string>>(new Map());
  readonly parties = input<readonly PartySummary[]>([]);
  /** Arriving from a party's own "Join party" button pre-selects it here instead of the form
   * defaulting to the first party in the list. */
  readonly preselectedPartyId = input('');

  readonly request = output<AffiliationRequestPayload>();
  readonly advance = output<void>();
  readonly reset = output<void>();

  protected readonly states = this.platform.states;
  protected readonly accountName = computed(() => this.session.account().name);

  protected readonly wizardStep = signal<WizardStep>('data');
  protected readonly selectedPartyId = signal('');
  protected readonly city = signal('');
  protected readonly voterRegistrationNumber = signal('');
  protected readonly electoralZone = signal('');
  protected readonly electoralSection = signal('');
  protected readonly electoralState = signal('');
  protected readonly electoralMunicipality = signal('');

  protected readonly identityPhotoFile = signal<File | null>(null);
  protected readonly identityPhotoPreviewUrl = computed(() => {
    const file = this.identityPhotoFile();
    return file ? URL.createObjectURL(file) : null;
  });
  protected readonly uploadingIdentityPhoto = signal(false);
  protected readonly submitError = signal('');

  private readonly identityPhotoInput = viewChild<ElementRef<HTMLInputElement>>('identityPhotoInput');

  constructor() {
    effect(() => {
      const preselected = this.preselectedPartyId();
      if (preselected) {
        this.selectedPartyId.set(preselected);
      }
    });
  }

  protected stepState(index: number): 'done' | 'active' | 'pending' {
    if (index < this.currentIndex()) {
      return 'done';
    }
    if (index === this.currentIndex()) {
      return 'active';
    }
    return 'pending';
  }

  protected stepDate(status: FiliationStatus): string | null {
    return this.stepDates().get(status) ?? null;
  }

  protected readonly dataStepValid = computed(
    () =>
      !!(this.selectedPartyId() || this.parties()[0]?.id) &&
      this.voterRegistrationNumber().trim().length > 0 &&
      this.electoralZone().trim().length > 0 &&
      this.electoralSection().trim().length > 0 &&
      this.electoralState().trim().length > 0 &&
      this.electoralMunicipality().trim().length > 0,
  );

  protected continueToIdentityStep(): void {
    if (!this.dataStepValid()) {
      return;
    }
    this.submitError.set('');
    this.wizardStep.set('identity');
  }

  protected backToDataStep(): void {
    this.wizardStep.set('data');
  }

  protected pickIdentityPhoto(): void {
    this.identityPhotoInput()?.nativeElement.click();
  }

  protected onIdentityPhotoSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      this.identityPhotoFile.set(file);
    }
  }

  protected onSubmit(): void {
    const partyId = this.selectedPartyId() || this.parties()[0]?.id;
    const photoFile = this.identityPhotoFile();
    if (!partyId || !photoFile) {
      this.submitError.set('missing');
      return;
    }
    this.submitError.set('');
    this.uploadingIdentityPhoto.set(true);
    this.media.upload(photoFile).subscribe({
      next: (identityPhotoUrl) => {
        this.uploadingIdentityPhoto.set(false);
        this.request.emit({
          partyId,
          city: this.city().trim(),
          voterRegistrationNumber: this.voterRegistrationNumber().trim(),
          electoralZone: this.electoralZone().trim(),
          electoralSection: this.electoralSection().trim(),
          electoralState: this.electoralState().trim(),
          electoralMunicipality: this.electoralMunicipality().trim(),
          identityPhotoUrl,
        });
      },
      error: () => {
        this.uploadingIdentityPhoto.set(false);
        this.submitError.set('upload-failed');
      },
    });
  }
}
