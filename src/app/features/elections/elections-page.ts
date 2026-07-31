import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
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

/** Some municipal races have 15+ elected vereadores — a card showing every one of them makes the
 * whole grid unreadable. The full roster is only ever a click away on the detail page. */
const CANDIDATE_PREVIEW_LIMIT = 6;

/** Public election calendar and candidates — visible to every account type, including visitors. */
@Component({
  selector: 'app-elections-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, FormsModule, InputText, Select, PageHeader, UiStat, UiCard, UiTag, UiAvatar, UiEmpty, TranslatePipe],
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
    { value: 'Nacional', label: this.translate.t('label.scope-federal', 'Federal') },
    { value: 'Estadual', label: this.translate.t('label.scope-state', 'State') },
    { value: 'Municipal', label: this.translate.t('label.scope-municipal', 'Municipal') },
  ];
  protected readonly scope = signal<ElectionScope | 'all'>('all');
  protected readonly search = signal('');

  protected readonly elections = computed<Election[]>(() => {
    const scope = this.scope();
    const term = this.search().trim().toLowerCase();
    const all = this.electionService.elections();
    let result = scope === 'all' ? all : all.filter((e) => e.scope === scope);
    if (term) {
      result = result.filter(
        (e) => e.title.toLowerCase().includes(term) || this.candidatesOf(e.id).some((c) => c.name.toLowerCase().includes(term)),
      );
    }
    return result;
  });

  /** Most recent year first, per election — a citizen thinks in terms of "the 2024 elections",
   * not a flat list sorted however the API happened to return it. */
  protected readonly years = computed(() => [...new Set(this.elections().map((e) => e.year))].sort((a, b) => b - a));

  protected electionsForYear(year: number): Election[] {
    return this.elections().filter((e) => e.year === year);
  }

  protected scopeSeverity(scope: ElectionScope): TagSeverity {
    return SCOPE_SEVERITY[scope];
  }

  protected candidatesOf(electionId: string) {
    return this.electionService.candidatesOf(electionId);
  }

  protected previewCandidatesOf(electionId: string) {
    return this.candidatesOf(electionId).slice(0, CANDIDATE_PREVIEW_LIMIT);
  }

  protected remainingCandidateCount(electionId: string): number {
    return Math.max(0, this.candidatesOf(electionId).length - CANDIDATE_PREVIEW_LIMIT);
  }
}
