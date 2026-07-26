import { HttpClient } from '@angular/common/http';
import { effect, inject, Injectable, signal } from '@angular/core';
import { Observable, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { SessionService } from './session.service';
import { TranslateService } from './translate.service';

export interface ConsentSetting {
  readonly id: string;
  readonly label: string;
  readonly description: string;
  /** Essential settings are required for the service and cannot be disabled. */
  readonly locked: boolean;
  enabled: boolean;
}

interface ConsentRecordResponseDto {
  readonly purpose: string;
  readonly granted: boolean;
}

interface AccountDeletionRequestResponseDto {
  readonly id: string;
}

interface DataExportRequestResponseDto {
  readonly id: string;
}

/** Static shell — `enabled` gets overwritten by the real per-account state on load. `id` is the
 * real purpose sent to/from privacy-compliance-service; label/description are display-only, so
 * translating them is always safe. */
function consentShell(translate: TranslateService): readonly Omit<ConsentSetting, 'enabled'>[] {
  return [
    {
      id: 'essential',
      label: translate.t('label.consent-essential', 'Essential account data'),
      description: translate.t(
        'label.consent-essential-desc',
        'Identity and security data required to run your account. Cannot be disabled.',
      ),
      locked: true,
    },
    {
      id: 'personalization',
      label: translate.t('label.consent-personalization', 'Personalized notifications'),
      description: translate.t(
        'label.consent-personalization-desc',
        'Use who you follow to tailor alerts about bills, votes and parties.',
      ),
      locked: false,
    },
    {
      id: 'analytics',
      label: translate.t('label.consent-analytics', 'Usage analytics'),
      description: translate.t('label.consent-analytics-desc', 'Anonymous usage metrics that help improve the platform.'),
      locked: false,
    },
    {
      id: 'marketing',
      label: translate.t('label.consent-marketing', 'Marketing & outreach'),
      description: translate.t(
        'label.consent-marketing-desc',
        'Receive updates about new features, civic campaigns and outreach initiatives.',
      ),
      locked: false,
    },
  ];
}

/**
 * LGPD-oriented privacy controls, backed by privacy-compliance-service: granular consent (real
 * per-purpose PUT/GET), data export (real request→processing→ready trail, delivered as a
 * client-side JSON download since no background export worker exists in this pass — see the
 * service's own "simulates the background worker" note) and account deletion (real
 * request→confirm trail before signing the user out).
 */
@Injectable({ providedIn: 'root' })
export class PrivacyService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly translate = inject(TranslateService);
  private readonly apiBase = `${environment.apiBaseUrl}/api/privacy`;

  private readonly _consents = signal<ConsentSetting[]>(consentShell(this.translate).map((c) => ({ ...c, enabled: c.locked })));
  readonly consents = this._consents.asReadonly();

  /** LGPD data-subject rights, surfaced to the user. */
  readonly rights: readonly string[] = [
    this.translate.t('label.lgpd-right-confirm', 'Confirm whether we process your personal data and access it.'),
    this.translate.t('label.lgpd-right-correct', 'Correct incomplete, inaccurate or outdated data.'),
    this.translate.t('label.lgpd-right-anonymize', 'Request anonymization, blocking or deletion of unnecessary data.'),
    this.translate.t('label.lgpd-right-port', 'Port your data to another service provider.'),
    this.translate.t('label.lgpd-right-withdraw', 'Withdraw consent at any time.'),
    this.translate.t('label.lgpd-right-informed', 'Be informed about with whom your data is shared.'),
  ];

  constructor() {
    // See DirectoryService's reloadFollowing for why this waits on session.ready() instead of
    // firing immediately — otherwise a hard refresh can permanently strand consents at defaults.
    effect(() => {
      if (this.session.ready()) {
        this.reload().subscribe();
      }
    });
  }

  reload(): Observable<ConsentSetting[]> {
    if (!this.session.isAuthenticated()) {
      return of(this._consents());
    }
    return this.http.get<ConsentRecordResponseDto[]>(`${this.apiBase}/consents`).pipe(
      map((records) => {
        const granted = new Map(records.map((r) => [r.purpose, r.granted]));
        return consentShell(this.translate).map(
          (shell): ConsentSetting => ({ ...shell, enabled: shell.locked ? true : (granted.get(shell.id) ?? false) }),
        );
      }),
      tap((consents) => this._consents.set(consents)),
    );
  }

  toggleConsent(id: string): void {
    const current = this._consents().find((c) => c.id === id);
    if (!current || current.locked) {
      return;
    }
    const granted = !current.enabled;
    this.http.put(`${this.apiBase}/consents`, { purpose: id, granted }).subscribe({
      next: () => this._consents.update((list) => list.map((c) => (c.id === id ? { ...c, enabled: granted } : c))),
    });
  }

  /** Builds a portable JSON snapshot of the account's data (right to portability), while also
   * recording the real request→processing→ready trail in privacy-compliance-service. */
  exportData(): void {
    this.http
      .post<DataExportRequestResponseDto>(`${this.apiBase}/data-export-requests`, {})
      .pipe(switchMap((req) => this.http.post(`${this.apiBase}/data-export-requests/${req.id}/start-processing`, {}).pipe(map(() => req))))
      .subscribe({
        next: (req) => {
          const payload = {
            exportedAt: new Date().toISOString(),
            account: this.session.account(),
            consents: this._consents(),
          };
          const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
          const url = URL.createObjectURL(blob);

          this.http.post(`${this.apiBase}/data-export-requests/${req.id}/ready`, { downloadUrl: url, expiresAt: null }).subscribe();

          const link = document.createElement('a');
          link.href = url;
          link.download = 'civicpulse-my-data.json';
          link.click();
          URL.revokeObjectURL(url);
        },
      });
  }

  /** Right to erasure — records the real request→confirm trail, then signs the user out. */
  deleteAccount(): void {
    this.http
      .post<AccountDeletionRequestResponseDto>(`${this.apiBase}/account-deletion-requests`, {})
      .pipe(switchMap((req) => this.http.post(`${this.apiBase}/account-deletion-requests/${req.id}/confirm`, {})))
      .subscribe({
        next: () => this.session.logout(),
        error: () => this.session.logout(),
      });
  }
}
