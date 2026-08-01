import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Observable, of } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { InputNumber } from 'primeng/inputnumber';
import { TextareaModule } from 'primeng/textarea';
import { Select } from 'primeng/select';
import { DatePicker } from 'primeng/datepicker';
import { FileSelectEvent, FileUpload } from 'primeng/fileupload';
import { ContributionGateway, FundraisingService } from '../../core/services/fundraising.service';
import { MediaService } from '../../core/services/media.service';
import { SessionService } from '../../core/services/session.service';
import { TranslateService } from '../../core/services/translate.service';
import { AlertsService } from '../../core/services/alerts.service';
import { Fundraiser, FundraiserCategory } from '../../core/models';
import { CanDirective } from '../../core/directives/can.directive';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiDialog } from '../../shared/ui/ui-dialog/ui-dialog';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiIconButton } from '../../shared/ui/ui-icon-button/ui-icon-button';
import { UiProgress } from '../../shared/ui/ui-progress/ui-progress';
import { UiTag } from '../../shared/ui/ui-tag/ui-tag';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { UiStat } from '../../shared/ui/ui-stat/ui-stat';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Fundraising hub for social causes and party initiatives (non-electoral). */
@Component({
  selector: 'app-fundraising-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CanDirective,
    PageHeader,
    UiDialog,
    UiIcon,
    UiIconButton,
    UiProgress,
    UiTag,
    UiButton,
    UiStat,
    FormsModule,
    InputText,
    InputNumber,
    TextareaModule,
    Select,
    DatePicker,
    FileUpload,
    TranslatePipe,
  ],
  templateUrl: './fundraising-page.html',
  styleUrl: './fundraising-page.scss',
})
export class FundraisingPage {
  private readonly fundraising = inject(FundraisingService);
  private readonly media = inject(MediaService);
  private readonly session = inject(SessionService);
  private readonly translate = inject(TranslateService);
  private readonly alerts = inject(AlertsService);

  protected readonly fundraisers = this.fundraising.fundraisers;
  protected readonly categories = this.fundraising.categories;
  protected readonly categoryOptions = this.categories.map((cat) => ({
    value: cat.category,
    label: this.translate.t(`category.${cat.category}`, cat.label),
  }));
  protected readonly totalRaised = this.fundraising.totalRaised;
  protected readonly totalSupporters = this.fundraising.totalSupporters;
  protected readonly isAuthenticated = this.session.isAuthenticated;

  protected readonly presets = [25, 50, 100];

  // ---- Checkout dialog: a real payment-checkout screen (amount + method), not inline buttons ----
  protected readonly checkoutFundraiser = signal<Fundraiser | null>(null);
  protected readonly checkoutAmount = signal<number | null>(null);
  protected readonly checkoutGateway = signal<ContributionGateway>('pix');
  protected readonly checkoutProcessing = signal(false);

  protected openCheckout(fundraiser: Fundraiser): void {
    this.detailsFundraiser.set(null);
    this.checkoutFundraiser.set(fundraiser);
    this.checkoutAmount.set(null);
    this.checkoutGateway.set('pix');
  }

  protected closeCheckout(): void {
    this.checkoutFundraiser.set(null);
  }

  protected pickPreset(amount: number): void {
    this.checkoutAmount.set(amount);
  }

  // ---- View-details dialog: full description/image, and a way into the same checkout above ----
  protected readonly detailsFundraiser = signal<Fundraiser | null>(null);

  protected openDetails(fundraiser: Fundraiser): void {
    this.detailsFundraiser.set(fundraiser);
  }

  protected closeDetails(): void {
    this.detailsFundraiser.set(null);
  }

  // ---- Create form state ----
  protected readonly showForm = signal(false);
  protected readonly title = signal('');
  protected readonly description = signal('');
  protected readonly category = signal<FundraiserCategory>('social');
  protected readonly goal = signal<number | null>(null);
  protected readonly deadline = signal<Date | null>(null);
  protected readonly imageFile = signal<File | null>(null);
  protected readonly imagePreviewUrl = computed(() => {
    const file = this.imageFile();
    return file ? URL.createObjectURL(file) : null;
  });

  protected onImageSelected(event: FileSelectEvent): void {
    const file = event.files[0];
    if (file) {
      this.imageFile.set(file);
    }
  }

  protected removeImage(): void {
    this.imageFile.set(null);
  }

  protected progress(f: Fundraiser): number {
    return f.goal > 0 ? (f.raised / f.goal) * 100 : 0;
  }

  protected money(value: number): string {
    return value.toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL',
      maximumFractionDigits: 0,
    });
  }

  protected categoryMeta(category: FundraiserCategory) {
    return this.fundraising.categoryMeta(category);
  }

  protected toggleForm(): void {
    this.showForm.update((v) => !v);
  }

  protected submit(): void {
    const goal = this.goal();
    if (!this.title().trim() || !goal || goal <= 0) {
      return;
    }
    const date = this.deadline();
    const deadline = date
      ? `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
      : 'Open-ended';
    const imageFile = this.imageFile();
    const upload$: Observable<string | null> = imageFile ? this.media.upload(imageFile) : of(null);
    upload$.subscribe((imageUrl) => {
      this.fundraising.create({
        title: this.title().trim(),
        description: this.description().trim(),
        category: this.category(),
        goal,
        deadline,
        imageUrl,
      });
      this.resetForm();
    });
  }

  /** Real Asaas Checkout — opens Asaas's own hosted invoice page in a new tab (Pix QR/copy-paste
   * or a card form depending on the chosen gateway), so this component never sees a card number
   * and the citizen keeps this page open. The final confirmation happens asynchronously via
   * payments-service's webhook once Asaas reports the payment settled (see
   * FundraisingService.startContribution) — there's no redirect-back to auto-detect success, so
   * the total updates once the citizen (or the next natural page reload) refreshes. */
  protected confirmCheckout(): void {
    const fundraiser = this.checkoutFundraiser();
    const amount = this.checkoutAmount();
    if (!fundraiser || !amount || amount <= 0) {
      return;
    }
    const gateway = this.checkoutGateway();
    this.checkoutProcessing.set(true);
    this.fundraising.startContribution(fundraiser.id, amount, gateway).subscribe({
      next: (checkoutUrl) => {
        this.checkoutProcessing.set(false);
        this.closeCheckout();
        if (checkoutUrl) {
          window.open(checkoutUrl, '_blank', 'noopener');
          this.alerts.push({
            category: 'campaign',
            icon: 'volunteer_activism',
            title: this.translate.t('title.checkout-opened', 'Complete your payment'),
            message: this.translate.t(
              'hint.checkout-opened',
              'We opened the secure payment page in a new tab. Once you complete it, refresh this page to see it reflected.',
            ),
            timeLabel: this.translate.t('label.just-now', 'Just now'),
            link: '/fundraising',
          });
        }
      },
      error: () => {
        this.checkoutProcessing.set(false);
        this.alerts.push({
          category: 'campaign',
          icon: 'error',
          title: this.translate.t('title.contribution-failed', 'Could not start payment'),
          message: this.translate.t(
            'hint.asaas-unavailable',
            'The payment gateway is temporarily unavailable — please try again shortly.',
          ),
          timeLabel: this.translate.t('label.just-now', 'Just now'),
          link: '/fundraising',
        });
      },
    });
  }

  private resetForm(): void {
    this.title.set('');
    this.description.set('');
    this.category.set('social');
    this.goal.set(null);
    this.deadline.set(null);
    this.imageFile.set(null);
    this.showForm.set(false);
  }
}
