import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { WalletService } from '../../core/services/wallet.service';
import { DirectoryService } from '../../core/services/directory.service';
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

  protected readonly card = this.wallet.card;
  protected readonly steps = this.wallet.steps;
  protected readonly status = this.wallet.status;
  protected readonly currentStepIndex = this.wallet.currentStepIndex;
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

  protected onPay(id: string): void {
    this.wallet.payFee(id);
  }
}
