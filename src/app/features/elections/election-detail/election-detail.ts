import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ElectionService } from '../../../core/services/election.service';
import { TagSeverity } from '../../../core/models';
import { PageHeader } from '../../../shared/ui/page-header/page-header';
import { UiCard } from '../../../shared/ui/ui-card/ui-card';
import { UiTag } from '../../../shared/ui/ui-tag/ui-tag';
import { UiAvatar } from '../../../shared/ui/ui-avatar/ui-avatar';
import { UiIcon } from '../../../shared/ui/ui-icon/ui-icon';
import { UiEmpty } from '../../../shared/ui/ui-empty/ui-empty';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';
import { TranslateService } from '../../../core/services/translate.service';

const SCOPE_SEVERITY: Record<string, TagSeverity> = {
  Nacional: 'primary',
  Estadual: 'secondary',
  Municipal: 'info',
};

/** Detail view for a single election — reached by clicking a card on the Elections page. */
@Component({
  selector: 'app-election-detail-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, PageHeader, UiCard, UiTag, UiAvatar, UiIcon, UiEmpty, TranslatePipe],
  templateUrl: './election-detail.html',
  styleUrl: './election-detail.scss',
})
export class ElectionDetailPage {
  private readonly electionService = inject(ElectionService);
  private readonly translate = inject(TranslateService);

  readonly id = input.required<string>();

  protected readonly election = computed(() => this.electionService.byId(this.id()));
  protected readonly candidates = computed(() => this.electionService.candidatesOf(this.id()));

  protected readonly isUpcoming = computed(() => {
    const e = this.election();
    return e ? this.electionService.isUpcoming(e) : false;
  });

  protected readonly statusLabel = computed(() =>
    this.isUpcoming()
      ? this.translate.t('label.upcoming', 'Upcoming')
      : this.translate.t('label.concluded', 'Concluded'),
  );

  protected scopeSeverity(scope: string): TagSeverity {
    return SCOPE_SEVERITY[scope] ?? 'neutral';
  }
}
