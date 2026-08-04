import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { DigitalCard } from '../../../../core/models';
import { UiQrcode } from '../../../../shared/ui/ui-qrcode/ui-qrcode';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

/** Digital membership card (carteira digital) with QR for verification. */
@Component({
  selector: 'app-membership-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiQrcode, TranslatePipe],
  template: `
    <div class="card" [class.card--inactive]="!active()">
      <div class="card__head">
        <div class="card__brand">
          <img src="logo.svg" alt="" width="22" height="22" class="card__brand-icon" />
          <span>IQORUM</span>
        </div>
        <span class="card__status">{{ (active() ? 'label.active-member' : 'label.pending') | translate: (active() ? 'Active member' : 'Pending') }}</span>
      </div>

      <div class="card__body">
        <div class="card__info">
          <div class="card__field-label">{{ 'label.member' | translate: 'Member' }}</div>
          <div class="card__holder">{{ card().holderName }}</div>

          <div class="card__grid">
            <div>
              <div class="card__field-label">{{ 'label.party' | translate: 'Party' }}</div>
              <div class="card__field-value">{{ card().party }} ({{ card().partyAcronym }})</div>
            </div>
            <div>
              <div class="card__field-label">{{ 'label.member-id' | translate: 'Member ID' }}</div>
              <div class="card__field-value">{{ card().memberId }}</div>
            </div>
            <div>
              <div class="card__field-label">{{ 'label.member-since' | translate: 'Member since' }}</div>
              <div class="card__field-value">{{ card().since }}</div>
            </div>
          </div>
        </div>

        <div class="card__qr">
          <ui-qrcode [data]="card().qrPayload" [size]="128" />
          <span class="card__qr-hint">{{ 'label.scan-to-verify' | translate: 'Scan to verify' }}</span>
        </div>
      </div>
    </div>
  `,
  styleUrl: './membership-card.scss',
})
export class MembershipCard {
  readonly card = input.required<DigitalCard>();
  readonly active = input(true);
}
