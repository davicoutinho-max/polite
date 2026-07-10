import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Select } from 'primeng/select';
import { InputText } from 'primeng/inputtext';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { DirectoryService, FilterOption, LEVEL_OPTIONS } from '../../core/services/directory.service';
import { SessionService } from '../../core/services/session.service';
import { PoliticianSummary } from '../../core/models';
import { InfiniteScrollDirective } from '../../core/directives/infinite-scroll.directive';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiAvatar } from '../../shared/ui/ui-avatar/ui-avatar';
import { UiTag } from '../../shared/ui/ui-tag/ui-tag';
import { CompactNumberPipe } from '../../shared/pipes/compact-number.pipe';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { TranslateService } from '../../core/services/translate.service';

type SortKey = 'followers' | 'name' | 'activity';

const PAGE_SIZE = 12;

/** Politician directory with search, advanced filters and infinite scroll. */
@Component({
  selector: 'app-politicians-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    FormsModule,
    Select,
    InputText,
    IconField,
    InputIcon,
    InfiniteScrollDirective,
    PageHeader,
    UiIcon,
    UiAvatar,
    UiTag,
    CompactNumberPipe,
    TranslatePipe,
  ],
  templateUrl: './politicians-page.html',
  styleUrl: './politicians-page.scss',
})
export class PoliticiansPage {
  private readonly directory = inject(DirectoryService);
  private readonly session = inject(SessionService);
  private readonly translate = inject(TranslateService);

  protected readonly canFollow = computed(() => this.session.can('follow'));

  // ---- Select options (with an "all" entry) ----
  protected readonly levelSelect: FilterOption[] = [
    { value: 'all', label: this.translate.t('label.all-levels', 'All levels') },
    ...LEVEL_OPTIONS.map((o) => ({ value: o.value, label: this.translate.t(`level.${o.value}`, o.label) })),
  ];
  protected readonly partySelect: FilterOption[] = [
    { value: 'all', label: this.translate.t('label.all-parties', 'All parties') },
    ...this.directory.partyOptions,
  ];
  protected readonly stateSelect = computed<FilterOption[]>(() => [
    { value: 'all', label: this.translate.t('label.all-states', 'All states') },
    ...this.directory.stateOptions(),
  ]);
  protected readonly sortSelect: FilterOption[] = [
    { value: 'followers', label: this.translate.t('label.most-followed', 'Most followed') },
    { value: 'activity', label: this.translate.t('label.most-active', 'Most active') },
    { value: 'name', label: this.translate.t('label.name-az', 'Name (A–Z)') },
  ];

  // ---- Filters ----
  protected readonly search = signal('');
  protected readonly level = signal('all');
  protected readonly party = signal('all');
  protected readonly state = signal('all');
  protected readonly sort = signal<SortKey>('followers');

  private readonly following = signal<ReadonlySet<string>>(new Set());
  protected readonly visibleCount = signal(PAGE_SIZE);

  protected readonly filtered = computed<PoliticianSummary[]>(() => {
    const q = this.search().trim().toLowerCase();
    const level = this.level();
    const party = this.party();
    const state = this.state();

    const list = this.directory.politicians().filter((p) => {
      if (level !== 'all' && p.level !== level) return false;
      if (party !== 'all' && p.partyId !== party) return false;
      if (state !== 'all' && p.state !== state) return false;
      if (q && !p.name.toLowerCase().includes(q) && !p.handle.toLowerCase().includes(q) && !p.partyAcronym.toLowerCase().includes(q)) {
        return false;
      }
      return true;
    });

    const sort = this.sort();
    return [...list].sort((a, b) => {
      if (sort === 'name') return a.name.localeCompare(b.name);
      if (sort === 'activity') return b.billsCount - a.billsCount;
      return b.followers - a.followers;
    });
  });

  protected readonly resultCount = computed(() => this.filtered().length);
  protected readonly visible = computed(() => this.filtered().slice(0, this.visibleCount()));
  protected readonly allLoaded = computed(() => this.visibleCount() >= this.resultCount());

  constructor() {
    // Reset paging whenever any filter changes.
    const filterKey = computed(
      () => `${this.search()}|${this.level()}|${this.party()}|${this.state()}|${this.sort()}`,
    );
    effect(() => {
      filterKey();
      this.visibleCount.set(PAGE_SIZE);
    });
  }

  protected setSearch(value: string): void {
    this.search.set(value);
  }

  protected loadMore(): void {
    if (!this.allLoaded()) {
      this.visibleCount.update((c) => c + PAGE_SIZE);
    }
  }

  protected isFollowing(id: string): boolean {
    return this.following().has(id);
  }

  protected toggleFollow(id: string): void {
    this.following.update((set) => {
      const next = new Set(set);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  protected clearFilters(): void {
    this.search.set('');
    this.level.set('all');
    this.party.set('all');
    this.state.set('all');
    this.sort.set('followers');
  }
}
