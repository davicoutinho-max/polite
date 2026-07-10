import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Select } from 'primeng/select';
import { ElectionService } from '../../core/services/election.service';
import { Election, ElectionScope, TagSeverity } from '../../core/models';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiStat } from '../../shared/ui/ui-stat/ui-stat';
import { UiCard } from '../../shared/ui/ui-card/ui-card';
import { UiTag } from '../../shared/ui/ui-tag/ui-tag';
import { UiAvatar } from '../../shared/ui/ui-avatar/ui-avatar';
import { UiEmpty } from '../../shared/ui/ui-empty/ui-empty';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { TranslateService } from '../../core/services/translate.service';

interface ScopeOption {
  readonly value: ElectionScope | 'all';
  readonly label: string;
}

const SCOPE_SEVERITY: Record<ElectionScope, TagSeverity> = {
  Nacional: 'primary',
  Estadual: 'secondary',
  Municipal: 'info',
};

/** Public election calendar and candidates — visible to every account type, including visitors. */
@Component({
  selector: 'app-elections-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, FormsModule, Select, PageHeader, UiStat, UiCard, UiTag, UiAvatar, UiEmpty, TranslatePipe],
  templateUrl: './elections-page.html',
  styleUrl: './elections-page.scss',
})
export class ElectionsPage {
  private readonly electionService = inject(ElectionService);
  private readonly translate = inject(TranslateService);

  protected readonly upcomingCount = this.electionService.upcomingCount;
  protected readonly totalCandidates = this.electionService.totalCandidates;
  protected readonly nextDate = this.electionService.nextDate;

  protected readonly scopeOptions: ScopeOption[] = [
    { value: 'all', label: this.translate.t('label.all-scopes', 'All scopes') },
    { value: 'Nacional', label: 'Nacional' },
    { value: 'Estadual', label: 'Estadual' },
    { value: 'Municipal', label: 'Municipal' },
  ];
  protected readonly scope = signal<ElectionScope | 'all'>('all');

  protected readonly elections = computed<Election[]>(() => {
    const scope = this.scope();
    const all = this.electionService.elections();
    return scope === 'all' ? all : all.filter((e) => e.scope === scope);
  });

  protected scopeSeverity(scope: ElectionScope): TagSeverity {
    return SCOPE_SEVERITY[scope];
  }

  protected candidatesOf(electionId: string) {
    return this.electionService.candidatesOf(electionId);
  }
}
