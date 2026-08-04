import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { InputText } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { Select } from 'primeng/select';
import { SessionService } from '../../../core/services/session.service';
import { PoliticianService } from '../../../core/services/politician.service';
import { PartyService } from '../../../core/services/party.service';
import { AlertsService } from '../../../core/services/alerts.service';
import { TranslateService } from '../../../core/services/translate.service';
import { PageHeader } from '../../../shared/ui/page-header/page-header';
import { UiButton } from '../../../shared/ui/ui-button/ui-button';
import { UiSection } from '../../../shared/ui/ui-section/ui-section';
import { UiIcon } from '../../../shared/ui/ui-icon/ui-icon';
import { UiSkeleton } from '../../../shared/ui/ui-skeleton/ui-skeleton';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

const SOCIAL_PLATFORM_OPTIONS: { value: string; label: string }[] = [
  { value: 'website', label: 'Website' },
  { value: 'instagram', label: 'Instagram' },
  { value: 'x', label: 'X' },
  { value: 'facebook', label: 'Facebook' },
  { value: 'youtube', label: 'YouTube' },
  { value: 'linkedin', label: 'LinkedIn' },
  { value: 'tiktok', label: 'TikTok' },
];

/** Self-service profile editing for politician/party accounts — reached either from the
 * registration flow (a citizen who claimed a government-sourced profile but chose not to import
 * its data starts here) or directly from "My profile" any time after. Politicians edit their
 * legislative dossier (education/profession/patrimony/phone/office + social links);
 * parties edit history/program/statute. Both write through endpoints that only existed
 * server-side until now — see PoliticianDossierController/PartyProfileController. */
@Component({
  selector: 'app-edit-profile-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, InputText, TextareaModule, Select, PageHeader, UiButton, UiSection, UiIcon, UiSkeleton, TranslatePipe],
  templateUrl: './edit-profile-page.html',
  styleUrl: './edit-profile-page.scss',
})
export class EditProfilePage {
  private readonly session = inject(SessionService);
  private readonly politicianService = inject(PoliticianService);
  private readonly partyService = inject(PartyService);
  private readonly alerts = inject(AlertsService);
  private readonly translate = inject(TranslateService);
  private readonly router = inject(Router);

  protected readonly accountType = computed(() => this.session.account().accountType);
  protected readonly isPolitician = computed(() => this.accountType() === 'politician');
  protected readonly isParty = computed(() => this.accountType() === 'party');

  protected readonly loading = signal(true);
  protected readonly saving = signal(false);

  protected readonly education = signal('');
  protected readonly profession = signal('');
  protected readonly patrimony = signal('');
  protected readonly dossierEmail = signal('');
  protected readonly phone = signal('');
  protected readonly officeDetail = signal('');
  /** Matches exactly what formatPhone produces — "(DD) XXXX-XXXX" (landline, 8 digits) or
   * "(DD) XXXXX-XXXX" (mobile, 9 digits). */
  protected readonly phonePattern = '^\\(\\d{2}\\) \\d{4,5}-\\d{4}$';

  protected readonly partyName = signal('');
  protected readonly partyAcronym = signal('');
  protected readonly partyNumber = signal('');
  protected readonly partyIdeology = signal('');
  protected readonly partyFoundedYear = signal('');
  protected readonly partyPresident = signal('');
  protected readonly history = signal('');
  protected readonly program = signal('');
  protected readonly statuteUrl = signal('');
  protected readonly videoUrl = signal('');

  protected readonly socialPlatformOptions = SOCIAL_PLATFORM_OPTIONS;
  protected readonly newLinkPlatform = signal('website');
  protected readonly newLinkLabel = signal('');
  protected readonly newLinkHandle = signal('');
  protected readonly newLinkUrl = signal('');
  protected readonly addingLink = signal(false);

  protected readonly socialLinks = computed(() => this.politicianService.politician().socialLinks);
  protected readonly mandates = computed(() => this.politicianService.politician().mandates);
  protected readonly team = computed(() => this.politicianService.politician().team);
  protected readonly careerMilestones = this.politicianService.career;

  protected readonly newMandateRole = signal('');
  protected readonly newMandatePeriod = signal('');
  protected readonly newMandateCurrent = signal(false);
  protected readonly addingMandate = signal(false);

  protected readonly newMemberName = signal('');
  protected readonly newMemberRole = signal('');
  protected readonly newMemberAvatarUrl = signal('');
  protected readonly addingMember = signal(false);

  protected readonly newMilestoneYear = signal('');
  protected readonly newMilestoneTitle = signal('');
  protected readonly newMilestoneDetail = signal('');
  protected readonly addingMilestone = signal(false);

  protected readonly speechesCount = signal('0');
  protected readonly interviewsCount = signal('0');
  protected readonly tripsCount = signal('0');
  protected readonly savingActivityCounts = signal(false);

  constructor() {
    const accountId = this.session.account().id;
    if (this.isPolitician()) {
      forkJoin([this.politicianService.load(accountId), this.politicianService.loadActivity(accountId), this.politicianService.loadCareer(accountId)]).subscribe({
        next: ([p, activity]) => {
          this.education.set(p.education);
          this.profession.set(p.profession);
          this.patrimony.set(p.patrimony);
          this.dossierEmail.set(p.email);
          this.phone.set(p.phone);
          this.officeDetail.set(p.office);
          this.speechesCount.set(String(activity.speeches));
          this.interviewsCount.set(String(activity.interviews));
          this.tripsCount.set(String(activity.trips));
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    } else if (this.isParty()) {
      this.partyService.load(accountId).subscribe({
        next: (p) => {
          this.partyName.set(p.name);
          this.partyAcronym.set(p.acronym);
          this.partyNumber.set(p.number ? String(p.number) : '');
          this.partyIdeology.set(p.ideology);
          this.partyFoundedYear.set(p.foundedYear !== null ? String(p.foundedYear) : '');
          this.partyPresident.set(p.president);
          this.history.set(p.history);
          this.program.set(p.program);
          this.statuteUrl.set(p.statuteUrl === '#' ? '' : p.statuteUrl);
          this.videoUrl.set(p.videoUrl);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    } else {
      this.loading.set(false);
    }
  }

  protected saveDossier(): void {
    const accountId = this.session.account().id;
    this.saving.set(true);
    this.politicianService
      .updateDossier(accountId, {
        education: this.education().trim(),
        profession: this.profession().trim(),
        patrimony: this.patrimony().trim(),
        email: this.dossierEmail().trim(),
        phone: this.phone().trim(),
        officeDetail: this.officeDetail().trim(),
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.notifySaved();
        },
        error: () => {
          this.saving.set(false);
          this.notifyError();
        },
      });
  }

  protected saveParty(): void {
    this.saving.set(true);
    forkJoin([
      this.partyService.updateProfile(this.history().trim(), this.program().trim(), this.statuteUrl().trim(), undefined, this.videoUrl().trim()),
      this.partyService.updatePartyDetails(
        this.partyName().trim(),
        this.partyAcronym().trim(),
        Number(this.partyNumber()) || 0,
        this.partyIdeology().trim(),
        this.partyFoundedYear().trim() ? Number(this.partyFoundedYear()) : null,
        this.partyPresident().trim(),
      ),
    ]).subscribe({
      next: () => {
        this.saving.set(false);
        this.notifySaved();
      },
      error: () => {
        this.saving.set(false);
        this.notifyError();
      },
    });
  }

  protected addSocialLink(): void {
    if (!this.newLinkLabel().trim() || !this.newLinkUrl().trim()) {
      return;
    }
    const accountId = this.session.account().id;
    this.addingLink.set(true);
    this.politicianService
      .addSocialLink(accountId, this.newLinkPlatform(), this.newLinkLabel().trim(), this.newLinkHandle().trim(), this.newLinkUrl().trim())
      .subscribe({
        next: () => {
          this.addingLink.set(false);
          this.newLinkLabel.set('');
          this.newLinkHandle.set('');
          this.newLinkUrl.set('');
          this.alerts.push({
            category: 'project',
            icon: 'check_circle',
            title: this.translate.t('title.link-added', 'Link added'),
            message: this.translate.t('hint.link-added', 'Your new social link is now public.'),
            timeLabel: this.translate.t('label.just-now', 'Just now'),
          });
        },
        error: () => this.addingLink.set(false),
      });
  }

  protected addMandate(): void {
    if (!this.newMandateRole().trim() || !this.newMandatePeriod().trim()) {
      return;
    }
    const accountId = this.session.account().id;
    this.addingMandate.set(true);
    this.politicianService.addMandate(accountId, this.newMandateRole().trim(), this.newMandatePeriod().trim(), this.newMandateCurrent()).subscribe({
      next: () => {
        this.addingMandate.set(false);
        this.newMandateRole.set('');
        this.newMandatePeriod.set('');
        this.newMandateCurrent.set(false);
        this.notifySuccess(this.translate.t('title.mandate-added', 'Mandate added'), this.translate.t('hint.mandate-added', 'It now shows on your public profile.'));
      },
      error: () => this.addingMandate.set(false),
    });
  }

  protected addTeamMember(): void {
    if (!this.newMemberName().trim() || !this.newMemberRole().trim()) {
      return;
    }
    const accountId = this.session.account().id;
    this.addingMember.set(true);
    this.politicianService.addTeamMember(accountId, this.newMemberName().trim(), this.newMemberRole().trim(), this.newMemberAvatarUrl().trim()).subscribe({
      next: () => {
        this.addingMember.set(false);
        this.newMemberName.set('');
        this.newMemberRole.set('');
        this.newMemberAvatarUrl.set('');
        this.notifySuccess(this.translate.t('title.team-member-added', 'Team member added'), this.translate.t('hint.team-member-added', 'They now show on your public profile.'));
      },
      error: () => this.addingMember.set(false),
    });
  }

  protected addMilestone(): void {
    const year = Number(this.newMilestoneYear());
    if (!year || !this.newMilestoneTitle().trim()) {
      return;
    }
    const accountId = this.session.account().id;
    this.addingMilestone.set(true);
    this.politicianService.addMilestone(accountId, year, this.newMilestoneTitle().trim(), this.newMilestoneDetail().trim()).subscribe({
      next: () => {
        this.addingMilestone.set(false);
        this.newMilestoneYear.set('');
        this.newMilestoneTitle.set('');
        this.newMilestoneDetail.set('');
        this.notifySuccess(this.translate.t('title.milestone-added', 'Milestone added'), this.translate.t('hint.milestone-added', 'It now shows on your career timeline.'));
      },
      error: () => this.addingMilestone.set(false),
    });
  }

  protected saveActivityCounts(): void {
    const accountId = this.session.account().id;
    this.savingActivityCounts.set(true);
    this.politicianService
      .updateActivityCounts(accountId, Number(this.speechesCount()) || 0, Number(this.interviewsCount()) || 0, Number(this.tripsCount()) || 0)
      .subscribe({
        next: () => {
          this.savingActivityCounts.set(false);
          this.notifySaved();
        },
        error: () => {
          this.savingActivityCounts.set(false);
          this.notifyError();
        },
      });
  }

  private notifySuccess(title: string, message: string): void {
    this.alerts.push({ category: 'project', icon: 'check_circle', title, message, timeLabel: this.translate.t('label.just-now', 'Just now') });
  }

  protected goToProfile(): void {
    const id = this.session.account().id;
    this.router.navigate(this.isParty() ? ['/party', id] : ['/profile', id]);
  }

  /** Live-formats as the user types — always puts the dash 4 digits from the end, which is what
   * naturally produces the right grouping for both an 8-digit landline ("XXXX-XXXX") and a
   * 9-digit mobile number ("XXXXX-XXXX") without needing to know in advance which one it is. */
  protected onPhoneInput(value: string): void {
    this.phone.set(this.formatPhone(value));
  }

  private formatPhone(value: string): string {
    const digits = value.replace(/\D/g, '').slice(0, 11);
    if (!digits) {
      return '';
    }
    return digits
      .replace(/^(\d{2})(\d)/, '($1) $2')
      .replace(/(\d)(\d{4})$/, '$1-$2');
  }

  private notifySaved(): void {
    this.alerts.push({
      category: 'project',
      icon: 'check_circle',
      title: this.translate.t('title.profile-saved', 'Profile updated'),
      message: this.translate.t('hint.profile-saved', 'Your changes were saved.'),
      timeLabel: this.translate.t('label.just-now', 'Just now'),
    });
  }

  private notifyError(): void {
    this.alerts.push({
      category: 'project',
      icon: 'error',
      title: this.translate.t('title.profile-save-failed', 'Could not save'),
      message: this.translate.t('hint.profile-save-failed', 'Please try again shortly.'),
      timeLabel: this.translate.t('label.just-now', 'Just now'),
    });
  }
}
