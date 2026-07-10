import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ParticipationService } from '../../core/services/participation.service';
import { SessionService } from '../../core/services/session.service';
import { ConsultationStance } from '../../core/models';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiTabs, UiTab } from '../../shared/ui/ui-tabs/ui-tabs';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { PetitionCard } from './components/petition-card/petition-card';
import { ConsultationCard } from './components/consultation-card/consultation-card';
import { SurveyCard } from './components/survey-card/survey-card';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Citizen participation hub: petitions, public consultations and surveys. */
@Component({
  selector: 'app-participation-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeader, UiTabs, UiIcon, PetitionCard, ConsultationCard, SurveyCard, TranslatePipe],
  templateUrl: './participation-page.html',
  styleUrl: './participation-page.scss',
})
export class ParticipationPage {
  private readonly participation = inject(ParticipationService);
  private readonly session = inject(SessionService);

  protected readonly isAuthenticated = this.session.isAuthenticated;

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
}
