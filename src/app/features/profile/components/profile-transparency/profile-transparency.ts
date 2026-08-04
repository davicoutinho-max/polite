import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputNumber } from 'primeng/inputnumber';
import { DatePicker } from 'primeng/datepicker';
import { TextareaModule } from 'primeng/textarea';
import { FileSelectEvent, FileUpload } from 'primeng/fileupload';
import { AccountabilityDisclosure, TransparencyReport } from '../../../../core/models';
import { MediaService } from '../../../../core/services/media.service';
import { PoliticianService } from '../../../../core/services/politician.service';
import { AlertsService } from '../../../../core/services/alerts.service';
import { TranslateService } from '../../../../core/services/translate.service';
import { UiSection } from '../../../../shared/ui/ui-section/ui-section';
import { UiStat } from '../../../../shared/ui/ui-stat/ui-stat';
import { UiProgress } from '../../../../shared/ui/ui-progress/ui-progress';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { UiIconButton } from '../../../../shared/ui/ui-icon-button/ui-icon-button';
import { UiButton } from '../../../../shared/ui/ui-button/ui-button';
import { UiTag } from '../../../../shared/ui/ui-tag/ui-tag';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

interface GlossaryItem {
  readonly key: string;
  readonly fallback: string;
  /** Matches legislative-service's accountability_category_options.code — what a disclosure
   * submission for this line item is filed under. */
  readonly categoryCode: string;
}

/** Federal deputy compensation structure — the real line items a disclosure can be filed
 * against (same for every profile, not per-politician data). See the Chamber of Deputies' own
 * CEAP regulation. */
const COMPENSATION_ITEMS: GlossaryItem[] = [
  { key: 'label.comp-subsidy', fallback: 'Subsídio (salário)', categoryCode: 'comp_subsidy' },
  { key: 'label.comp-13th-salary', fallback: '13º salário', categoryCode: 'comp_13th_salary' },
  { key: 'label.comp-housing-allowance', fallback: 'Auxílio-moradia (ou apartamento funcional)', categoryCode: 'comp_housing_allowance' },
  { key: 'label.comp-ceap', fallback: 'Cota para o Exercício da Atividade Parlamentar (CEAP)', categoryCode: 'comp_ceap' },
  { key: 'label.comp-office-budget', fallback: 'Verba de Gabinete', categoryCode: 'comp_office_budget' },
  { key: 'label.comp-per-diem', fallback: 'Diárias (em missões oficiais)', categoryCode: 'comp_per_diem' },
  { key: 'label.comp-official-flights', fallback: 'Passagens aéreas oficiais', categoryCode: 'comp_official_flights' },
  { key: 'label.comp-medical-care', fallback: 'Atendimento médico pelo Departamento Médico da Câmara', categoryCode: 'comp_medical_care' },
  { key: 'label.comp-health-plan', fallback: 'Plano de saúde (conforme regras da Câmara)', categoryCode: 'comp_health_plan' },
  { key: 'label.comp-pension', fallback: 'Previdência parlamentar (opcional)', categoryCode: 'comp_pension' },
];

const CEAP_ITEMS: GlossaryItem[] = [
  { key: 'label.ceap-fuel', fallback: 'Combustível', categoryCode: 'ceap_fuel' },
  { key: 'label.ceap-flights', fallback: 'Passagens aéreas', categoryCode: 'ceap_flights' },
  { key: 'label.ceap-lodging', fallback: 'Hospedagem', categoryCode: 'ceap_lodging' },
  { key: 'label.ceap-office-rent', fallback: 'Aluguel de escritório', categoryCode: 'ceap_office_rent' },
  { key: 'label.ceap-telephony', fallback: 'Telefonia', categoryCode: 'ceap_telephony' },
  { key: 'label.ceap-internet', fallback: 'Internet', categoryCode: 'ceap_internet' },
  { key: 'label.ceap-postal', fallback: 'Correios', categoryCode: 'ceap_postal' },
  { key: 'label.ceap-office-supplies', fallback: 'Material de escritório', categoryCode: 'ceap_office_supplies' },
  { key: 'label.ceap-vehicle-rental', fallback: 'Locação de veículos', categoryCode: 'ceap_vehicle_rental' },
  { key: 'label.ceap-vehicle-maintenance', fallback: 'Manutenção de veículos', categoryCode: 'ceap_vehicle_maintenance' },
  { key: 'label.ceap-taxi-transport', fallback: 'Táxi e transporte', categoryCode: 'ceap_taxi_transport' },
  { key: 'label.ceap-consulting', fallback: 'Consultorias', categoryCode: 'ceap_consulting' },
  { key: 'label.ceap-outreach', fallback: 'Divulgação da atividade parlamentar', categoryCode: 'ceap_outreach' },
  { key: 'label.ceap-content-production', fallback: 'Produção de conteúdo', categoryCode: 'ceap_content_production' },
  { key: 'label.ceap-subscriptions', fallback: 'Assinaturas de jornais e revistas', categoryCode: 'ceap_subscriptions' },
  { key: 'label.ceap-graphic-services', fallback: 'Serviços gráficos', categoryCode: 'ceap_graphic_services' },
  { key: 'label.ceap-equipment', fallback: 'Equipamentos para o exercício do mandato', categoryCode: 'ceap_equipment' },
];

const OFFICE_BUDGET_ITEMS: GlossaryItem[] = [
  { key: 'label.gabinete-aides', fallback: 'Contratação de assessores parlamentares', categoryCode: 'gabinete_aides' },
  { key: 'label.gabinete-chief-of-staff', fallback: 'Chefia de gabinete', categoryCode: 'gabinete_chief_of_staff' },
  { key: 'label.gabinete-secretaries', fallback: 'Secretários parlamentares', categoryCode: 'gabinete_secretaries' },
];

/** Transparency tab: public-money KPIs, an expense breakdown, and — one upload affordance per
 * real compensation/CEAP/office-budget line item — an AI-verified "prestação de contas"
 * submission workflow, broken down by month/year (see legislative-service's
 * AccountabilityDisclosure javadoc). The upload button only renders for the profile's own owner;
 * everyone can see submitted disclosures and open their attached document. */
@Component({
  selector: 'app-profile-transparency',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    NgTemplateOutlet,
    FormsModule,
    InputNumber,
    DatePicker,
    TextareaModule,
    FileUpload,
    UiSection,
    UiStat,
    UiProgress,
    UiIcon,
    UiIconButton,
    UiButton,
    UiTag,
    TranslatePipe,
  ],
  templateUrl: './profile-transparency.html',
  styleUrl: './profile-transparency.scss',
})
export class ProfileTransparency {
  private readonly politicianService = inject(PoliticianService);
  private readonly media = inject(MediaService);
  private readonly translate = inject(TranslateService);
  private readonly alerts = inject(AlertsService);

  readonly report = input.required<TransparencyReport>();
  readonly politicianId = input('');
  readonly isOwnProfile = input(false);

  protected readonly disclosures = this.politicianService.accountabilityDisclosures;

  protected readonly compensationItems = COMPENSATION_ITEMS;
  protected readonly ceapItems = CEAP_ITEMS;
  protected readonly officeBudgetItems = OFFICE_BUDGET_ITEMS;

  protected glossaryLabel(item: GlossaryItem): string {
    return this.translate.t(item.key, item.fallback);
  }

  /** Only one item's submission form is expanded at a time. */
  protected readonly activeCategory = signal<string | null>(null);
  protected readonly declaredAmount = signal<number | null>(null);
  protected readonly documentFile = signal<File | null>(null);
  protected readonly periodDate = signal<Date | null>(null);
  protected readonly notes = signal('');
  protected readonly submitting = signal(false);

  private readonly disclosuresByCategory = computed(() => {
    const map = new Map<string, AccountabilityDisclosure[]>();
    for (const disclosure of this.disclosures()) {
      const list = map.get(disclosure.category) ?? [];
      list.push(disclosure);
      map.set(disclosure.category, list);
    }
    for (const list of map.values()) {
      list.sort((a, b) => b.periodYear - a.periodYear || b.periodMonth - a.periodMonth);
    }
    return map;
  });

  /** Every submission for this line item, newest period first — a politician reports the same
   * category again every month, so unlike a single "current status" this is a real list. */
  protected disclosuresFor(categoryCode: string): AccountabilityDisclosure[] {
    return this.disclosuresByCategory().get(categoryCode) ?? [];
  }

  protected isActive(categoryCode: string): boolean {
    return this.activeCategory() === categoryCode;
  }

  protected startSubmission(categoryCode: string): void {
    this.activeCategory.set(categoryCode);
    this.declaredAmount.set(null);
    this.documentFile.set(null);
    this.periodDate.set(null);
    this.notes.set('');
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

  protected readonly canSubmit = computed(
    () => !!this.declaredAmount() && this.declaredAmount()! > 0 && !!this.documentFile() && !!this.periodDate(),
  );

  protected periodLabel(disclosure: AccountabilityDisclosure): string {
    return `${String(disclosure.periodMonth).padStart(2, '0')}/${disclosure.periodYear}`;
  }

  protected submit(): void {
    const category = this.activeCategory();
    const amount = this.declaredAmount();
    const file = this.documentFile();
    const period = this.periodDate();
    if (!category || !amount || amount <= 0 || !file || !period) {
      return;
    }
    const periodYear = period.getFullYear();
    const periodMonth = period.getMonth() + 1;

    this.submitting.set(true);
    this.media.upload(file).subscribe({
      next: (documentUrl) => {
        this.politicianService
          .submitAccountabilityDisclosure(category, periodMonth, periodYear, Math.round(amount * 100), documentUrl, this.notes())
          .subscribe({
            next: (disclosure) => {
              this.submitting.set(false);
              this.activeCategory.set(null);
              if (disclosure.status === 'approved') {
                this.notifySuccess(
                  this.translate.t('title.disclosure-approved', 'Disclosure approved'),
                  this.translate.t('hint.disclosure-approved', 'The AI reviewer confirmed your document matches the declared amount.'),
                );
              } else {
                this.notifyError(this.translate.t('title.disclosure-rejected', 'Disclosure rejected'), disclosure.aiFeedback);
              }
            },
            error: () => {
              this.submitting.set(false);
              this.notifyError(
                this.translate.t('title.upload-failed', 'Upload failed'),
                this.translate.t('hint.upload-failed', 'Please try again shortly.'),
              );
            },
          });
      },
      error: () => {
        this.submitting.set(false);
        this.notifyError(this.translate.t('title.upload-failed', 'Upload failed'), this.translate.t('hint.upload-failed', 'Please try again shortly.'));
      },
    });
  }

  private notifySuccess(title: string, message: string): void {
    this.alerts.push({ category: 'project', icon: 'check_circle', title, message, timeLabel: this.translate.t('label.just-now', 'Just now') });
  }

  private notifyError(title: string, message: string): void {
    this.alerts.push({ category: 'project', icon: 'error', title, message, timeLabel: this.translate.t('label.just-now', 'Just now') });
  }

  protected money(cents: number): string {
    return (cents / 100).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }
}
