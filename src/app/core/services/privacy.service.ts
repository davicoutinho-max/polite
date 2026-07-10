import { inject, Injectable, signal } from '@angular/core';
import { SessionService } from './session.service';

export interface ConsentSetting {
  readonly id: string;
  readonly label: string;
  readonly description: string;
  /** Essential settings are required for the service and cannot be disabled. */
  readonly locked: boolean;
  enabled: boolean;
}

/**
 * LGPD-oriented privacy controls: granular consent, data portability and the
 * right to erasure. State is in-memory (demo), but the flow mirrors the real
 * obligations a platform handling personal data must offer.
 */
@Injectable({ providedIn: 'root' })
export class PrivacyService {
  private readonly session = inject(SessionService);

  private readonly _consents = signal<ConsentSetting[]>([
    {
      id: 'essential',
      label: 'Essential account data',
      description: 'Identity and security data required to run your account. Cannot be disabled.',
      locked: true,
      enabled: true,
    },
    {
      id: 'notifications',
      label: 'Personalized notifications',
      description: 'Use who you follow to tailor alerts about bills, votes and parties.',
      locked: false,
      enabled: true,
    },
    {
      id: 'analytics',
      label: 'Usage analytics',
      description: 'Anonymous usage metrics that help improve the platform.',
      locked: false,
      enabled: true,
    },
    {
      id: 'research',
      label: 'Opinion research',
      description: 'Allow your survey answers to be used in aggregated, anonymized research.',
      locked: false,
      enabled: false,
    },
  ]);

  readonly consents = this._consents.asReadonly();

  /** LGPD data-subject rights, surfaced to the user. */
  readonly rights: readonly string[] = [
    'Confirm whether we process your personal data and access it.',
    'Correct incomplete, inaccurate or outdated data.',
    'Request anonymization, blocking or deletion of unnecessary data.',
    'Port your data to another service provider.',
    'Withdraw consent at any time.',
    'Be informed about with whom your data is shared.',
  ];

  toggleConsent(id: string): void {
    this._consents.update((list) =>
      list.map((c) => (c.id === id && !c.locked ? { ...c, enabled: !c.enabled } : c)),
    );
  }

  /** Builds a portable JSON snapshot of the account's data (right to portability). */
  exportData(): void {
    const payload = {
      exportedAt: new Date().toISOString(),
      account: this.session.account(),
      consents: this._consents(),
    };
    const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'civicpulse-my-data.json';
    link.click();
    URL.revokeObjectURL(url);
  }

  /** Right to erasure — signs the user out in this demo. */
  deleteAccount(): void {
    this.session.logout();
  }
}
