import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Select } from 'primeng/select';
import { InputText } from 'primeng/inputtext';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { DirectoryService, FilterOption, SPECTRUM_OPTIONS } from '../../core/services/directory.service';
import { SessionService } from '../../core/services/session.service';
import { PartySummary } from '../../core/models';
import { InfiniteScrollDirective } from '../../core/directives/infinite-scroll.directive';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { UiTag } from '../../shared/ui/ui-tag/ui-tag';
import { CompactNumberPipe } from '../../shared/pipes/compact-number.pipe';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { TranslateService } from '../../core/services/translate.service';

type SortKey = 'members' | 'name' | 'founded';

const PAGE_SIZE = 9;

/** Party directory with search, advanced filters and infinite scroll. */
@Component({
  selector: 'app-parties-page',
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
    UiTag,
    CompactNumberPipe,
    TranslatePipe,
  ],
  templateUrl: './parties-page.html',
  styleUrl: './parties-page.scss',
})
export class PartiesPage {
  private readonly directory = inject(DirectoryService);
  private readonly session = inject(SessionService);
  private readonly translate = inject(TranslateService);

  protected readonly spectrumOptions = SPECTRUM_OPTIONS.map((o) => ({
    value: o.value,
    label: this.translate.t(`spectrum.${o.value}`, o.label),
  }));
  protected readonly canFollow = computed(() => this.session.can('follow'));

  protected readonly spectrumSelect: FilterOption[] = [
    { value: 'all', label: this.translate.t('label.all-spectrums', 'All spectrums') },
    ...this.spectrumOptions,
  ];
  protected readonly sortSelect: FilterOption[] = [
    { value: 'members', label: this.translate.t('label.largest', 'Largest') },
    { value: 'founded', label: this.translate.t('label.newest', 'Newest') },
    { value: 'name', label: this.translate.t('label.name-az', 'Name (A–Z)') },
  ];

  protected readonly search = signal('');
  protected readonly spectrum = signal('all');
  protected readonly sort = signal<SortKey>('members');

  protected readonly visibleCount = signal(PAGE_SIZE);

  protected readonly filtered = computed<PartySummary[]>(() => {
    const q = this.search().trim().toLowerCase();
    const spectrum = this.spectrum();

    const list = this.directory.parties().filter((p) => {
      if (spectrum !== 'all' && p.spectrum !== spectrum) return false;
      if (q && !p.name.toLowerCase().includes(q) && !p.acronym.toLowerCase().includes(q) && !p.ideology.toLowerCase().includes(q)) {
        return false;
      }
      return true;
    });

    const sort = this.sort();
    return [...list].sort((a, b) => {
      if (sort === 'name') return a.name.localeCompare(b.name);
      if (sort === 'founded') return b.founded - a.founded;
      return b.members - a.members;
    });
  });

  protected readonly resultCount = computed(() => this.filtered().length);
  protected readonly visible = computed(() => this.filtered().slice(0, this.visibleCount()));
  protected readonly allLoaded = computed(() => this.visibleCount() >= this.resultCount());

  protected spectrumLabel(value: string): string {
    return this.spectrumOptions.find((o) => o.value === value)?.label ?? value;
  }

  constructor() {
    const filterKey = computed(() => `${this.search()}|${this.spectrum()}|${this.sort()}`);
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
    return this.directory.isFollowing('party', id);
  }

  protected toggleFollow(id: string): void {
    const action$ = this.isFollowing(id) ? this.directory.unfollow('party', id) : this.directory.follow('party', id);
    action$.subscribe({ error: () => undefined });
  }

  protected clearFilters(): void {
    this.search.set('');
    this.spectrum.set('all');
    this.sort.set('members');
  }
}
