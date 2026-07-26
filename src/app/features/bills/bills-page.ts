import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { LEGISLATIVE_BILL_TYPES, LegislativeBillSummary } from '../../core/models';
import { LegislativeOpenDataService } from '../../core/services/legislative-open-data.service';
import { TranslateService } from '../../core/services/translate.service';
import { BillCard } from '../../shared/legislative/bill-card/bill-card';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Matches LegislativeOpenDataService's own page size — used here only to guess whether a
 * "load more" click is likely to reveal anything (see `PAGE_SIZE` there for the real source of
 * truth on how many results a page holds). */
const PAGE_SIZE = 8;

/** Real bills (Projetos de Lei), pulled live from Câmara dos Deputados / Senado Federal's own
 * open-data APIs. Loads grouped by bill type up front so there's always something to read; the
 * search box only narrows it down, it's never required. Per-bill history/Ask AI live in
 * BillCard. See LegislativeOpenDataService. */
@Component({
  selector: 'app-bills-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, InputText, PageHeader, BillCard, UiButton, UiIcon, TranslatePipe],
  templateUrl: './bills-page.html',
  styleUrl: './bills-page.scss',
})
export class BillsPage {
  private readonly legislativeOpenData = inject(LegislativeOpenDataService);
  private readonly translate = inject(TranslateService);

  protected readonly billTypes = LEGISLATIVE_BILL_TYPES;

  /** 'all' shows every type's section (the original default); picking one type narrows both the
   * default grouped view and any active search down to just that type — no extra API calls, since
   * every type is already loaded up front by `listRecentByType()`. */
  protected readonly selectedType = signal('all');

  protected readonly visibleTypes = computed(() => (this.selectedType() === 'all' ? this.billTypes : this.billTypes.filter((t) => t.code === this.selectedType())));

  protected readonly visibleSearchResults = computed(() => {
    const results = this.searchResults() ?? [];
    return this.selectedType() === 'all' ? results : results.filter((b) => b.typeLabel === this.selectedType());
  });

  protected readonly keyword = signal('');
  protected readonly loading = signal(false);
  protected readonly error = signal('');

  /** The default view: recent bills of every type, loaded without any search. */
  protected readonly groupedBills = signal<Record<string, LegislativeBillSummary[]>>({});
  protected readonly loadingMoreType = signal<string | null>(null);
  protected readonly exhaustedTypes = signal<ReadonlySet<string>>(new Set());
  private readonly typePage: Record<string, number> = {};

  /** Set once the citizen searches; while non-null, the flat search results replace the grouped
   * view. Clearing the search box goes back to the grouped view. */
  protected readonly searchResults = signal<LegislativeBillSummary[] | null>(null);
  protected readonly isSearching = computed(() => this.searchResults() !== null);
  protected readonly loadingMoreSearch = signal(false);
  protected readonly searchExhausted = signal(false);
  private searchPage = 1;

  constructor() {
    this.loadGrouped();
  }

  private loadGrouped(): void {
    this.loading.set(true);
    this.error.set('');
    this.legislativeOpenData.listRecentByType().subscribe({
      next: (grouped) => {
        this.loading.set(false);
        this.groupedBills.set(grouped);
        const exhausted = new Set<string>();
        for (const type of this.billTypes) {
          this.typePage[type.code] = 1;
          if ((grouped[type.code]?.length ?? 0) < PAGE_SIZE) {
            exhausted.add(type.code);
          }
        }
        this.exhaustedTypes.set(exhausted);
      },
      error: () => {
        this.loading.set(false);
        this.error.set(this.translate.t('error.bills-search-failed', 'Could not reach Câmara/Senado open-data services right now.'));
      },
    });
  }

  protected loadMoreForType(typeCode: string): void {
    const nextPage = (this.typePage[typeCode] ?? 1) + 1;
    this.loadingMoreType.set(typeCode);
    this.legislativeOpenData.listByType(typeCode, nextPage).subscribe({
      next: (bills) => {
        this.loadingMoreType.set(null);
        const previousCount = this.groupedBills()[typeCode]?.length ?? 0;
        this.typePage[typeCode] = nextPage;
        this.groupedBills.update((g) => ({ ...g, [typeCode]: bills }));
        if (bills.length <= previousCount) {
          this.exhaustedTypes.update((s) => new Set(s).add(typeCode));
        }
      },
      error: () => {
        this.loadingMoreType.set(null);
        this.exhaustedTypes.update((s) => new Set(s).add(typeCode));
      },
    });
  }

  protected search(): void {
    const term = this.keyword().trim();
    if (!term) {
      this.searchResults.set(null);
      return;
    }
    this.searchPage = 1;
    this.loading.set(true);
    this.error.set('');
    this.legislativeOpenData.searchBills(term, 1).subscribe({
      next: (bills) => {
        this.loading.set(false);
        this.searchResults.set(bills);
        this.searchExhausted.set(bills.length < PAGE_SIZE);
        if (bills.length === 0) {
          this.error.set(this.translate.t('error.bills-none-found', 'No related bills were found on Câmara/Senado for this topic.'));
        }
      },
      error: () => {
        this.loading.set(false);
        this.searchResults.set([]);
        this.error.set(this.translate.t('error.bills-search-failed', 'Could not reach Câmara/Senado open-data services right now.'));
      },
    });
  }

  protected loadMoreSearch(): void {
    const term = this.keyword().trim();
    if (!term) {
      return;
    }
    const nextPage = this.searchPage + 1;
    this.loadingMoreSearch.set(true);
    this.legislativeOpenData.searchBills(term, nextPage).subscribe({
      next: (bills) => {
        this.loadingMoreSearch.set(false);
        const previousCount = this.searchResults()?.length ?? 0;
        this.searchPage = nextPage;
        this.searchResults.set(bills);
        if (bills.length <= previousCount) {
          this.searchExhausted.set(true);
        }
      },
      error: () => {
        this.loadingMoreSearch.set(false);
        this.searchExhausted.set(true);
      },
    });
  }

  protected clearSearch(): void {
    this.keyword.set('');
    this.searchResults.set(null);
    this.error.set('');
  }

  protected billsForType(typeCode: string): LegislativeBillSummary[] {
    return this.groupedBills()[typeCode] ?? [];
  }

  protected isExhausted(typeCode: string): boolean {
    return this.exhaustedTypes().has(typeCode);
  }
}
