import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FundraisingService } from '../../core/services/fundraising.service';
import { SessionService } from '../../core/services/session.service';
import { Fundraiser, FundraiserCategory } from '../../core/models';
import { CanDirective } from '../../core/directives/can.directive';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiProgress } from '../../shared/ui/ui-progress/ui-progress';
import { UiTag } from '../../shared/ui/ui-tag/ui-tag';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { UiStat } from '../../shared/ui/ui-stat/ui-stat';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Fundraising hub for social causes and party initiatives (non-electoral). */
@Component({
  selector: 'app-fundraising-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CanDirective, PageHeader, UiIcon, UiProgress, UiTag, UiButton, UiStat, TranslatePipe],
  templateUrl: './fundraising-page.html',
  styleUrl: './fundraising-page.scss',
})
export class FundraisingPage {
  private readonly fundraising = inject(FundraisingService);
  private readonly session = inject(SessionService);

  protected readonly fundraisers = this.fundraising.fundraisers;
  protected readonly categories = this.fundraising.categories;
  protected readonly totalRaised = this.fundraising.totalRaised;
  protected readonly totalSupporters = this.fundraising.totalSupporters;
  protected readonly isAuthenticated = this.session.isAuthenticated;

  protected readonly presets = [25, 50, 100];

  // ---- Create form state ----
  protected readonly showForm = signal(false);
  protected readonly title = signal('');
  protected readonly description = signal('');
  protected readonly category = signal<FundraiserCategory>('social');
  protected readonly goal = signal<number | null>(null);
  protected readonly deadline = signal('');

  protected progress(f: Fundraiser): number {
    return f.goal > 0 ? (f.raised / f.goal) * 100 : 0;
  }

  protected money(value: number): string {
    return value.toLocaleString('en-US', {
      style: 'currency',
      currency: 'USD',
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
    this.fundraising.create({
      title: this.title().trim(),
      description: this.description().trim(),
      category: this.category(),
      goal,
      deadline: this.deadline().trim() || 'Open-ended',
    });
    this.resetForm();
  }

  protected contribute(id: string, amount: number): void {
    this.fundraising.contribute(id, amount);
  }

  private resetForm(): void {
    this.title.set('');
    this.description.set('');
    this.category.set('social');
    this.goal.set(null);
    this.deadline.set('');
    this.showForm.set(false);
  }
}
