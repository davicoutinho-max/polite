import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputNumber } from 'primeng/inputnumber';
import { FileSelectEvent, FileUpload } from 'primeng/fileupload';
import { ACCOUNTABILITY_CATEGORIES, AccountabilityCategory, AccountabilityDisclosure, TransparencyReport } from '../../../../core/models';
import { MediaService } from '../../../../core/services/media.service';
import { PoliticianService } from '../../../../core/services/politician.service';
import { TranslateService } from '../../../../core/services/translate.service';
import { UiSection } from '../../../../shared/ui/ui-section/ui-section';
import { UiStat } from '../../../../shared/ui/ui-stat/ui-stat';
import { UiProgress } from '../../../../shared/ui/ui-progress/ui-progress';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { UiButton } from '../../../../shared/ui/ui-button/ui-button';
import { UiTag } from '../../../../shared/ui/ui-tag/ui-tag';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

/** Transparency tab: public-money KPIs, an expense breakdown, and — one section per real
 * accountability category — an AI-verified "prestação de contas" submission workflow (see
 * legislative-service's AccountabilityDisclosure javadoc). The submission form only renders for
 * the profile's own owner; everyone else sees the same categories read-only. */
@Component({
  selector: 'app-profile-transparency',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, InputNumber, FileUpload, UiSection, UiStat, UiProgress, UiIcon, UiButton, UiTag, TranslatePipe],
  templateUrl: './profile-transparency.html',
  styleUrl: './profile-transparency.scss',
})
export class ProfileTransparency {
  private readonly politicianService = inject(PoliticianService);
  private readonly media = inject(MediaService);
  private readonly translate = inject(TranslateService);

  readonly report = input.required<TransparencyReport>();
  readonly politicianId = input('');
  readonly isOwnProfile = input(false);

  protected readonly categories = ACCOUNTABILITY_CATEGORIES;
  protected readonly disclosures = this.politicianService.accountabilityDisclosures;

  /** Only one category's submission form is expanded at a time. */
  protected readonly activeCategory = signal<AccountabilityCategory | null>(null);
  protected readonly declaredAmount = signal<number | null>(null);
  protected readonly documentFile = signal<File | null>(null);
  protected readonly submitting = signal(false);

  protected readonly latestByCategory = computed(() => {
    const map = new Map<AccountabilityCategory, AccountabilityDisclosure>();
    for (const disclosure of this.disclosures()) {
      if (!map.has(disclosure.category)) {
        map.set(disclosure.category, disclosure);
      }
    }
    return map;
  });

  protected categoryLabel(category: AccountabilityCategory): string {
    const meta = this.categories.find((c) => c.value === category);
    return this.translate.t(`label.accountability-${category}`, meta?.label ?? category);
  }

  protected categoryIcon(category: AccountabilityCategory): string {
    return this.categories.find((c) => c.value === category)?.icon ?? 'receipt_long';
  }

  protected latestOf(category: AccountabilityCategory): AccountabilityDisclosure | null {
    return this.latestByCategory().get(category) ?? null;
  }

  protected isActive(category: AccountabilityCategory): boolean {
    return this.activeCategory() === category;
  }

  protected startSubmission(category: AccountabilityCategory): void {
    this.activeCategory.set(category);
    this.declaredAmount.set(null);
    this.documentFile.set(null);
  }

  protected cancelSubmission(): void {
    this.activeCategory.set(null);
  }

  protected onDocumentSelected(event: FileSelectEvent): void {
    const file = event.files[0];
    if (file) {
      this.documentFile.set(file);
    }
  }

  protected readonly canSubmit = computed(() => !!this.declaredAmount() && this.declaredAmount()! > 0 && !!this.documentFile());

  protected submit(): void {
    const category = this.activeCategory();
    const amount = this.declaredAmount();
    const file = this.documentFile();
    if (!category || !amount || amount <= 0 || !file) {
      return;
    }
    this.submitting.set(true);
    this.media.upload(file).subscribe({
      next: (documentUrl) => {
        this.politicianService.submitAccountabilityDisclosure(category, Math.round(amount * 100), documentUrl).subscribe({
          next: () => {
            this.submitting.set(false);
            this.activeCategory.set(null);
          },
          error: () => this.submitting.set(false),
        });
      },
      error: () => this.submitting.set(false),
    });
  }

  protected money(cents: number): string {
    return (cents / 100).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }
}
