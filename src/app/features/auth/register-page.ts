import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { InputText } from 'primeng/inputtext';
import { forkJoin, Observable } from 'rxjs';
import { SessionService } from '../../core/services/session.service';
import { DirectoryService } from '../../core/services/directory.service';
import { PlatformService } from '../../core/services/platform.service';
import { PartyService } from '../../core/services/party.service';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiAvatar } from '../../shared/ui/ui-avatar/ui-avatar';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { TranslateService } from '../../core/services/translate.service';
import { digitsOnly, formatDocumentNumber, isValidDocumentNumber } from '../../shared/utils/br-documents';

/** Derives a handle from the display name (identity-service requires one, but this form only
 * collects a name) — lowercased, non-alphanumeric stripped, with a short random suffix to avoid
 * collisions since two people can share a name. */
function deriveHandle(name: string): string {
  const slug = name
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[^a-z0-9]+/g, '')
    .slice(0, 20);
  const suffix = Math.floor(Math.random() * 10000);
  return `${slug || 'user'}${suffix}`;
}

interface MatchCandidate {
  readonly accountId: string;
  readonly name: string;
  readonly avatarUrl: string | null;
  readonly subtitle: string;
}

interface SearchResultItem {
  readonly kind: 'politician' | 'party';
  readonly id: string;
  readonly name: string;
  readonly avatarUrl: string | null;
  readonly subtitle: string;
}

type Step = 'form' | 'checking' | 'confirm' | 'search' | 'submitting' | 'invite-loading' | 'invite';

interface PartyInvitePrefill {
  readonly name: string;
  readonly acronym: string;
  readonly number: number;
  readonly ideology: string;
  readonly president: string;
}

interface PoliticianInvitePrefill {
  readonly name: string;
  readonly roleTitle: string;
  readonly state: string;
  readonly partyId: string;
}

/**
 * Sign-up screen — a real citizen registration against identity-service, extended with an
 * opt-in government-data import: after the basics, we check whether the CPF/CNPJ typed matches
 * a real, unclaimed politician/party profile a government-data sync already built (only ever
 * fires for federal deputies — the one source with a real CPF on file, see
 * SessionService.checkDocument's javadoc). If not, the citizen can search the directory by name
 * themselves (covers senators, state/municipal politicians and every party, none of which carry
 * a real document number). Either way, nothing is imported without an explicit confirmation —
 * declining still ties the account to the real profile (so login/identity is correct), but sends
 * the citizen to the profile-editing screen instead of assuming the government data is right.
 *
 * A `?token=` query param takes over the whole flow instead: it means someone (a platform admin
 * for a party, or a party for a politician) already vetted this person's identity and issued them
 * an invite — see RegistrationToken's javadoc on identity-service. That skips the CPF/CNPJ
 * auto-check and manual-search steps entirely (there's no government data to match against; the
 * account is brand new) and always lands on `/profile/edit` afterward so they fill in everything
 * themselves, same as declining an import in the citizen flow.
 */
@Component({
  selector: 'app-register-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, UiButton, UiIcon, UiAvatar, InputText, TranslatePipe],
  templateUrl: './register-page.html',
  styleUrl: './auth-page.scss',
})
export class RegisterPage {
  private readonly session = inject(SessionService);
  private readonly directory = inject(DirectoryService);
  private readonly platform = inject(PlatformService);
  private readonly partyService = inject(PartyService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);

  protected readonly name = signal('');
  protected readonly email = signal('');
  protected readonly documentNumber = signal('');
  protected readonly password = signal('');
  protected readonly error = signal('');
  protected readonly submitting = signal(false);

  protected readonly step = signal<Step>('form');
  protected readonly matchCandidate = signal<MatchCandidate | null>(null);
  protected readonly searchQuery = signal('');
  protected readonly searching = signal(false);
  protected readonly searchResults = signal<SearchResultItem[]>([]);
  protected readonly searchedOnce = signal(false);

  // ---- Invite-token redemption (party or politician) ----
  protected readonly inviteToken = signal<string | null>(null);
  protected readonly inviteAccountType = signal<'party' | 'politician' | null>(null);
  protected readonly invitePartyPrefill = signal<PartyInvitePrefill | null>(null);
  protected readonly invitePoliticianPrefill = signal<PoliticianInvitePrefill | null>(null);
  protected readonly handle = signal('');

  /** Manual token entry on the plain citizen form — most people arrive here via the emailed
   * link's `?token=` (handled below in the constructor), but that's not the only way someone
   * could end up with a token in hand (forwarded/copied separately, email client stripping query
   * params, etc.), so the field has to be reachable without the URL param too. */
  protected readonly showTokenField = signal(false);
  protected readonly manualToken = signal('');
  protected readonly checkingManualToken = signal(false);

  constructor() {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (token) {
      this.redeemInviteToken(token);
    }
  }

  protected toggleTokenField(): void {
    this.showTokenField.update((v) => !v);
    this.error.set('');
  }

  protected submitManualToken(): void {
    const token = this.manualToken().trim();
    if (!token) {
      return;
    }
    this.checkingManualToken.set(true);
    this.redeemInviteToken(token, () => this.checkingManualToken.set(false));
  }

  /** Validates the token (read-only preview, never consumes it — see
   * SessionService.validateRegistrationToken's javadoc) and, if valid, switches the whole page
   * into the invite-redemption flow. `onInvalid` lets the manual-entry path recover in place
   * instead of resetting all the way back to the plain form. */
  private redeemInviteToken(token: string, onInvalid?: () => void): void {
    this.inviteToken.set(token);
    this.error.set('');
    if (!onInvalid) {
      this.step.set('invite-loading');
    }
    this.session.validateRegistrationToken(token).subscribe((preview) => {
      onInvalid?.();
      if (!preview || (preview.accountType !== 'party' && preview.accountType !== 'politician')) {
        this.error.set(this.translate.t('error.invalid-invite', 'This invite link is invalid or has expired.'));
        this.inviteToken.set(null);
        this.step.set('form');
        return;
      }
      this.inviteAccountType.set(preview.accountType);
      this.email.set(preview.targetEmail ?? '');
      const prefill = preview.prefillData ? JSON.parse(preview.prefillData) : {};
      if (preview.accountType === 'party') {
        this.invitePartyPrefill.set(prefill as PartyInvitePrefill);
      } else {
        this.invitePoliticianPrefill.set(prefill as PoliticianInvitePrefill);
      }
      this.step.set('invite');
    });
  }

  /** Display name shown on the invite-confirmation step, regardless of account type. */
  protected readonly invitePrefillName = () => this.invitePartyPrefill()?.name ?? this.invitePoliticianPrefill()?.name ?? '';

  /** A party's CNPJ is now part of the admin-vetted invite (see PartyInvitePrefill on the
   * backend) — it's never re-typed here. Only a politician's personal CPF is still self-provided,
   * since that's the individual's own document, not something the inviting party should be
   * declaring on their behalf. */
  protected readonly showDocumentField = computed(() => this.inviteAccountType() === 'politician');

  protected onHandleInput(raw: string): void {
    this.handle.set(raw.toLowerCase().replace(/[^a-z0-9._]/g, ''));
  }

  protected submitInviteRedemption(): void {
    const token = this.inviteToken();
    const accountType = this.inviteAccountType();
    if (!token || !accountType) {
      return;
    }
    const needsDocument = this.showDocumentField();
    if (!this.handle().trim() || !this.email().trim() || !this.password().trim() || (needsDocument && !this.documentNumber().trim())) {
      this.error.set(
        needsDocument
          ? this.translate.t('error.fill-registration-fields', 'Fill in your name, email, CPF/CNPJ and password to create an account.')
          : this.translate.t('error.fill-registration-fields-no-document', 'Fill in your handle, email and password to create an account.'),
      );
      return;
    }
    if (needsDocument && !isValidDocumentNumber(this.documentNumber())) {
      this.error.set(this.translate.t('error.invalid-document', 'Enter a valid CPF or CNPJ.'));
      return;
    }
    if (this.password().trim().length < 8) {
      this.error.set(this.translate.t('error.password-too-short', 'Password must be at least 8 characters.'));
      return;
    }

    this.error.set('');
    this.step.set('submitting');
    this.submitting.set(true);
    const handle = this.handle().trim();
    const email = this.email().trim();
    const password = this.password().trim();

    const redemption$: Observable<unknown> =
      accountType === 'party'
        ? this.platform.createParty({ registrationToken: token, handle, email, password })
        : this.partyService.registerPolitician(this.invitePoliticianPrefill()!.partyId, {
            registrationToken: token,
            handle,
            email,
            password,
            documentNumber: digitsOnly(this.documentNumber()),
          });

    redemption$.subscribe({
      next: () => {
        this.session.login(email, password).subscribe({
          next: () => {
            this.submitting.set(false);
            this.router.navigateByUrl('/profile/edit');
          },
          error: () => {
            this.submitting.set(false);
            this.router.navigateByUrl('/login');
          },
        });
      },
      error: () => {
        this.submitting.set(false);
        this.step.set('invite');
        this.error.set(
          this.translate.t('error.registration-failed', 'Could not create your account. That email or document may already be registered.'),
        );
      },
    });
  }

  protected onDocumentInput(raw: string): void {
    this.documentNumber.set(formatDocumentNumber(raw));
  }

  /** Step 1 -> 2: basics look valid, so check whether the document matches a real, unclaimed
   * profile before ever creating anything. */
  protected continueFromBasics(): void {
    if (!this.name().trim() || !this.email().trim() || !this.documentNumber().trim() || !this.password().trim()) {
      this.error.set(
        this.translate.t('error.fill-registration-fields', 'Fill in your name, email, CPF/CNPJ and password to create an account.'),
      );
      return;
    }
    if (!isValidDocumentNumber(this.documentNumber())) {
      this.error.set(this.translate.t('error.invalid-document', 'Enter a valid CPF or CNPJ.'));
      return;
    }
    if (this.password().trim().length < 8) {
      this.error.set(this.translate.t('error.password-too-short', 'Password must be at least 8 characters.'));
      return;
    }

    this.error.set('');
    this.step.set('checking');
    this.session.checkDocument(digitsOnly(this.documentNumber())).subscribe({
      next: (result) => {
        if (result.matched && result.accountId) {
          this.matchCandidate.set({
            accountId: result.accountId,
            name: result.name ?? '',
            avatarUrl: result.avatarUrl,
            subtitle: result.accountType === 'party' ? this.translate.t('label.party', 'Party') : this.translate.t('label.politician', 'Politician'),
          });
          this.step.set('confirm');
        } else {
          this.step.set('search');
        }
      },
      // A failed pre-check shouldn't block registration outright — fall through to the manual
      // search step, same as "no automatic match found".
      error: () => this.step.set('search'),
    });
  }

  protected runSearch(): void {
    const query = this.searchQuery().trim();
    if (!query) {
      return;
    }
    this.searching.set(true);
    this.searchedOnce.set(true);
    forkJoin([this.directory.searchPoliticiansByName(query), this.directory.searchPartiesByName(query)]).subscribe({
      next: ([politicians, parties]) => {
        this.searching.set(false);
        this.searchResults.set([
          ...politicians.map(
            (p): SearchResultItem => ({
              kind: 'politician',
              id: p.id,
              name: p.name,
              avatarUrl: p.avatarUrl,
              subtitle: [p.office, p.state].filter(Boolean).join(' · '),
            }),
          ),
          ...parties.map((p): SearchResultItem => ({ kind: 'party', id: p.id, name: p.name, avatarUrl: p.logoUrl, subtitle: p.acronym })),
        ]);
      },
      error: () => {
        this.searching.set(false);
        this.searchResults.set([]);
      },
    });
  }

  protected pickSearchResult(item: SearchResultItem): void {
    this.matchCandidate.set({
      accountId: item.id,
      name: item.name,
      avatarUrl: item.avatarUrl,
      subtitle: item.kind === 'party' ? this.translate.t('label.party', 'Party') : this.translate.t('label.politician', 'Politician'),
    });
    this.step.set('confirm');
  }

  /** "Not me" from the confirmation card — auto-match has nowhere else to go but the manual
   * search; a rejected search pick goes back to search results (already fetched). */
  protected rejectMatch(): void {
    this.matchCandidate.set(null);
    this.step.set('search');
  }

  protected skipToCitizen(): void {
    this.matchCandidate.set(null);
    this.finishRegistration(null, false);
  }

  protected confirmImport(importGovernmentData: boolean): void {
    const candidate = this.matchCandidate();
    if (!candidate) {
      return;
    }
    this.finishRegistration(candidate.accountId, importGovernmentData);
  }

  private finishRegistration(claimAccountId: string | null, importedGovernmentData: boolean): void {
    this.step.set('submitting');
    this.submitting.set(true);
    const handle = deriveHandle(this.name().trim());
    this.session
      .register(this.name().trim(), handle, this.email().trim(), this.password().trim(), digitsOnly(this.documentNumber()), claimAccountId ?? undefined)
      .subscribe({
        next: () => {
          this.submitting.set(false);
          if (claimAccountId && !importedGovernmentData) {
            this.router.navigateByUrl('/profile/edit');
          } else {
            this.router.navigateByUrl('/feed');
          }
        },
        error: () => {
          this.submitting.set(false);
          this.step.set('form');
          this.error.set(
            this.translate.t('error.registration-failed', 'Could not create your account. That email or document may already be registered.'),
          );
        },
      });
  }
}
