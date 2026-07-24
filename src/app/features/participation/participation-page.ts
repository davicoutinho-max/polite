import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { ParticipationService } from '../../core/services/participation.service';
import { SessionService } from '../../core/services/session.service';
import { TranslateService } from '../../core/services/translate.service';
import { ConsultationStance } from '../../core/models';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiTabs, UiTab } from '../../shared/ui/ui-tabs/ui-tabs';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { PetitionCard } from './components/petition-card/petition-card';
import { ConsultationCard } from './components/consultation-card/consultation-card';
import { SurveyCard } from './components/survey-card/survey-card';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Citizen participation hub: petitions, public consultations and surveys. Politicians/parties
 * (the `create-participation` permission) additionally get a per-tab "Create" form here — the
 * same three instruments, just authored instead of answered. */
@Component({
  selector: 'app-participation-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, InputText, PageHeader, UiTabs, UiIcon, UiButton, PetitionCard, ConsultationCard, SurveyCard, TranslatePipe],
  templateUrl: './participation-page.html',
  styleUrl: './participation-page.scss',
})
export class ParticipationPage {
  private readonly participation = inject(ParticipationService);
  private readonly session = inject(SessionService);
  private readonly translate = inject(TranslateService);

  protected readonly isAuthenticated = this.session.isAuthenticated;
  protected readonly canCreate = computed(() => this.session.can('create-participation'));

  protected readonly petitions = this.participation.petitions;
  protected readonly consultations = this.participation.consultations;
  protected readonly surveys = this.participation.surveys;

  protected readonly tabs: UiTab[] = [
    { id: 'petitions', label: 'Petitions', key: 'tab.petitions', icon: 'edit_document' },
    { id: 'consultations', label: 'Consultations', key: 'tab.consultations', icon: 'forum' },
    { id: 'surveys', label: 'Surveys', key: 'tab.surveys', icon: 'ballot' },
  ];
  protected readonly active = signal('petitions');

  protected onSign(id: string): void {
    this.participation.signPetition(id);
  }

  protected onStance(event: { id: string; stance: ConsultationStance }): void {
    this.participation.setStance(event.id, event.stance);
  }

  protected onVote(event: { surveyId: string; optionId: string }): void {
    this.participation.vote(event.surveyId, event.optionId);
  }

  protected onTabChange(id: string): void {
    this.active.set(id);
    this.showCreateForm.set(false);
  }

  // ---- Create (politician/party only) ----
  protected readonly showCreateForm = signal(false);
  protected readonly createSubmitting = signal(false);
  protected readonly createError = signal('');

  protected readonly petitionTitle = signal('');
  protected readonly petitionSummary = signal('');
  protected readonly petitionCategory = signal('');
  protected readonly petitionGoal = signal(100);
  protected readonly petitionDeadline = signal('');

  protected readonly consultationTitle = signal('');
  protected readonly consultationDescription = signal('');
  protected readonly consultationDeadline = signal('');

  protected readonly surveyQuestion = signal('');
  protected readonly surveyContext = signal('');
  protected readonly surveyOptions = signal<string[]>(['', '']);

  protected toggleCreateForm(): void {
    this.showCreateForm.update((v) => !v);
    this.createError.set('');
  }

  protected setSurveyOption(index: number, value: string): void {
    this.surveyOptions.update((list) => list.map((o, i) => (i === index ? value : o)));
  }

  protected addSurveyOption(): void {
    this.surveyOptions.update((list) => [...list, '']);
  }

  protected removeSurveyOption(index: number): void {
    this.surveyOptions.update((list) => (list.length > 2 ? list.filter((_, i) => i !== index) : list));
  }

  protected submitCreate(): void {
    switch (this.active()) {
      case 'petitions':
        this.submitPetition();
        break;
      case 'consultations':
        this.submitConsultation();
        break;
      case 'surveys':
        this.submitSurvey();
        break;
    }
  }

  private submitPetition(): void {
    const title = this.petitionTitle().trim();
    if (!title) {
      this.createError.set(this.translate.t('error.petition-required', 'Fill in the petition title.'));
      return;
    }
    this.createError.set('');
    this.createSubmitting.set(true);
    this.participation
      .createPetition(title, this.petitionSummary().trim(), this.petitionCategory().trim(), this.petitionGoal(), this.petitionDeadline() || null)
      .subscribe({
        next: () => {
          this.createSubmitting.set(false);
          this.petitionTitle.set('');
          this.petitionSummary.set('');
          this.petitionCategory.set('');
          this.petitionGoal.set(100);
          this.petitionDeadline.set('');
          this.showCreateForm.set(false);
        },
        error: () => {
          this.createSubmitting.set(false);
          this.createError.set(this.translate.t('error.petition-failed', 'Could not create the petition.'));
        },
      });
  }

  private submitConsultation(): void {
    const title = this.consultationTitle().trim();
    if (!title) {
      this.createError.set(this.translate.t('error.consultation-required', 'Fill in the consultation title.'));
      return;
    }
    this.createError.set('');
    this.createSubmitting.set(true);
    this.participation.createConsultation(title, this.consultationDescription().trim(), this.consultationDeadline() || null).subscribe({
      next: () => {
        this.createSubmitting.set(false);
        this.consultationTitle.set('');
        this.consultationDescription.set('');
        this.consultationDeadline.set('');
        this.showCreateForm.set(false);
      },
      error: () => {
        this.createSubmitting.set(false);
        this.createError.set(this.translate.t('error.consultation-failed', 'Could not create the consultation.'));
      },
    });
  }

  private submitSurvey(): void {
    const question = this.surveyQuestion().trim();
    const options = this.surveyOptions().map((o) => o.trim()).filter((o) => o.length > 0);
    if (!question || options.length < 2) {
      this.createError.set(this.translate.t('error.survey-required', 'Fill in the question and at least two options.'));
      return;
    }
    this.createError.set('');
    this.createSubmitting.set(true);
    this.participation.createSurvey(question, this.surveyContext().trim(), options).subscribe({
      next: () => {
        this.createSubmitting.set(false);
        this.surveyQuestion.set('');
        this.surveyContext.set('');
        this.surveyOptions.set(['', '']);
        this.showCreateForm.set(false);
      },
      error: () => {
        this.createSubmitting.set(false);
        this.createError.set(this.translate.t('error.survey-failed', 'Could not create the survey.'));
      },
    });
  }
}
