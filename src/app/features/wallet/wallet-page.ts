import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FeePaymentGateway, WalletService } from '../../core/services/wallet.service';
import { DirectoryService } from '../../core/services/directory.service';
import { AlertsService } from '../../core/services/alerts.service';
import { TagSeverity } from '../../core/models';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiSection } from '../../shared/ui/ui-section/ui-section';
import { UiTag } from '../../shared/ui/ui-tag/ui-tag';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { MembershipCard } from './components/membership-card/membership-card';
import { FiliationStatusComponent, AffiliationRequestPayload } from './components/filiation-status/filiation-status';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { TranslateService } from '../../core/services/translate.service';

const FEE_SEVERITY: Record<string, TagSeverity> = {
  paid: 'success',
  pending: 'warning',
  overdue: 'danger',
};

/** Digital Wallet page: membership card, affiliation flow and monthly fees. */
@Component({
  selector: 'app-wallet-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeader, UiSection, UiTag, UiIcon, UiButton, MembershipCard, FiliationStatusComponent, TranslatePipe],
  templateUrl: './wallet-page.html',
  styleUrl: './wallet-page.scss',
})
export class WalletPage {
  private readonly wallet = inject(WalletService);
  private readonly directory = inject(DirectoryService);
  private readonly translate = inject(TranslateService);
  private readonly alerts = inject(AlertsService);
  private readonly route = inject(ActivatedRoute);

  protected readonly checkoutProcessing = signal(false);

  /** Set when arriving from a party's "Join party" button (?partyId=…) so the affiliation
   * form starts with that party already selected instead of defaulting to the first one. */
  protected readonly preselectedPartyId = this.route.snapshot.queryParamMap.get('partyId') ?? '';

  protected readonly card = this.wallet.card;
  protected readonly steps = this.wallet.steps;
  protected readonly status = this.wallet.status;
  protected readonly currentStepIndex = this.wallet.currentStepIndex;
  protected readonly stepDates = this.wallet.stepDates;
  protected readonly isAffiliated = this.wallet.isAffiliated;
  protected readonly fees = this.wallet.fees;
  protected readonly pendingFee = this.wallet.pendingFee;

  protected readonly parties = this.directory.parties;

  protected feeSeverity(status: string): TagSeverity {
    return FEE_SEVERITY[status] ?? 'neutral';
  }

  protected feeStatusLabel(status: string): string {
    return this.translate.t(`status.${status}`, status);
  }

  protected onRequest(payload: AffiliationRequestPayload): void {
    this.wallet.requestFiliation(payload.partyId, payload.city);
  }

  protected onAdvance(): void {
    this.wallet.advance();
  }

  protected onReset(): void {
    this.wallet.reset();
  }

  /** Real Asaas Checkout — opens Asaas's own hosted invoice page in a new tab (Pix QR/copy-paste
   * or a card form depending on `gateway`), so this component never sees a card number. The fee
   * only shows as paid once Asaas's webhook confirms the payment settled — there's no
   * redirect-back to auto-detect success, so refresh this page afterwards to see it reflected. */
  protected payFee(id: string, gateway: FeePaymentGateway): void {
    this.checkoutProcessing.set(true);
    this.wallet.startFeeCheckout(id, gateway).subscribe({
      next: (checkoutUrl) => {
        this.checkoutProcessing.set(false);
        if (checkoutUrl) {
          window.open(checkoutUrl, '_blank', 'noopener');
          this.alerts.push({
            category: 'party',
            icon: 'payments',
            title: this.translate.t('title.checkout-opened', 'Complete your payment'),
            message: this.translate.t(
              'hint.checkout-opened',
              'We opened the secure payment page in a new tab. Once you complete it, refresh this page to see it reflected.',
            ),
            timeLabel: this.translate.t('label.just-now', 'Just now'),
            link: '/wallet',
          });
        }
      },
      error: () => {
        this.checkoutProcessing.set(false);
        this.alerts.push({
          category: 'party',
          icon: 'error',
          title: this.translate.t('title.payment-failed', 'Could not start payment'),
          message: this.translate.t(
            'hint.asaas-unavailable',
            'The payment gateway is temporarily unavailable — please try again shortly.',
          ),
          timeLabel: this.translate.t('label.just-now', 'Just now'),
          link: '/wallet',
        });
      },
    });
  }
}
