import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { from, map, mergeMap } from 'rxjs';
import { LEGISLATIVE_BILL_TYPES, LegislativeBillSummary } from '../../core/models';
import { FilterOption } from '../../core/services/directory.service';
import { LegislativeOpenDataService } from '../../core/services/legislative-open-data.service';
import { TranslateService } from '../../core/services/translate.service';
import { BillCard } from '../../shared/legislative/bill-card/bill-card';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

type SortKey = 'recent' | 'party';

/** Caps how many "resolve this bill's author party" calls run at once — enrichment happens for
 * every bill already on screen, and firing 20-40 of these at once against Câmara's API in one
 * burst is the kind of thing that gets a client rate-limited for no good reason. */
const PARTY_ENRICHMENT_CONCURRENCY = 3;

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
  imports: [FormsModule, InputText, Select, PageHeader, BillCard, UiButton, UiIcon, TranslatePipe],
  templateUrl: './bills-page.html',
  styleUrl: './bills-page.scss',
})
export class BillsPage {
  private readonly legislativeOpenData = inject(LegislativeOpenDataService);
  private readonly translate = inject(TranslateService);

  protected readonly billTypes = LEGISLATIVE_BILL_TYPES.map((t) => ({ code: t.code, label: this.translate.t(`bill-type.${t.code}`, t.label) }));

  protected readonly typeFilterOptions: FilterOption[] = [
    { value: 'all', label: this.translate.t('label.all-bill-types', 'All types') },
    ...this.billTypes.map((t) => ({ value: t.code, label: t.label })),
  ];

  /** 'all' shows every type's section (the original default); picking one type narrows both the
   * default grouped view and any active search down to just that type — no extra API calls, since
   * every type is already loaded up front by `listRecentByType()`. */
  protected readonly selectedType = signal('all');

  protected readonly visibleTypes = computed(() => (this.selectedType() === 'all' ? this.billTypes : this.billTypes.filter((t) => t.code === this.selectedType())));

  // ---- Author name/party filter/sort — resolved lazily per bill after each load, see enrichParties() ----
  protected readonly resolvedAuthors = signal<ReadonlyMap<string, { readonly name: string | null; readonly party: string | null }>>(new Map());
  private readonly resolvedParties = computed(() => new Map([...this.resolvedAuthors()].map(([key, v]) => [key, v.party])));
  protected readonly partyFilter = signal('all');
  protected readonly sortKey = signal<SortKey>('recent');

  protected readonly partyFilterOptions = computed<FilterOption[]>(() => {
    const parties = [...new Set([...this.resolvedParties().values()].filter((p): p is string => p !== null))].sort();
    return [{ value: 'all', label: this.translate.t('label.all-parties', 'All parties') }, ...parties.map((p) => ({ value: p, label: p }))];
  });

  protected readonly sortOptions: FilterOption[] = [
    { value: 'recent', label: this.translate.t('label.most-recent', 'Most recent') },
    { value: 'party', label: this.translate.t('label.party-az', 'Party (A–Z)') },
  ];

  private partyKeyOf(bill: LegislativeBillSummary): string {
    return `${bill.source}:${bill.id}`;
  }

  private applyPartyFilterAndSort(bills: LegislativeBillSummary[]): LegislativeBillSummary[] {
    const resolved = this.resolvedAuthors();
    // Merge in whatever name/party each bill has resolved to so far — lets bill-card show them
    // directly without every caller needing its own lookup against resolvedAuthors().
    let result = bills.map((b): LegislativeBillSummary => {
      const info = resolved.get(this.partyKeyOf(b));
      return { ...b, authorName: info?.name, authorParty: info?.party };
    });

    const filter = this.partyFilter();
    if (filter !== 'all') {
      result = result.filter((b) => b.authorParty === filter);
    }
    if (this.sortKey() === 'party') {
      result = [...result].sort((a, b) => {
        if (!a.authorParty && !b.authorParty) return 0;
        if (!a.authorParty) return 1;
        if (!b.authorParty) return -1;
        return a.authorParty.localeCompare(b.authorParty);
      });
    }
    return result;
  }

  /** Fires one author lookup (name + party) per bill not already resolved, capped at
   * PARTY_ENRICHMENT_CONCURRENCY in flight at once, and merges each result into
   * `resolvedAuthors` as it arrives — the list renders immediately and author names/party
   * tags/filter options fill in progressively rather than blocking on the whole batch. */
  private enrichParties(bills: LegislativeBillSummary[]): void {
    const resolved = this.resolvedAuthors();
    const unresolved = bills.filter((b) => !resolved.has(this.partyKeyOf(b)));
    if (unresolved.length === 0) {
      return;
    }
    from(unresolved)
      .pipe(
        mergeMap(
          (bill) => this.legislativeOpenData.resolveAuthorInfo(bill.source, bill.id).pipe(map((info) => ({ bill, info }))),
          PARTY_ENRICHMENT_CONCURRENCY,
        ),
      )
      .subscribe(({ bill, info }) => {
        this.resolvedAuthors.update((map) => new Map(map).set(this.partyKeyOf(bill), info));
      });
  }

  protected readonly visibleSearchResults = computed(() => {
    const results = this.searchResults() ?? [];
    const byType = this.selectedType() === 'all' ? results : results.filter((b) => b.typeLabel === this.selectedType());
    return this.applyPartyFilterAndSort(byType);
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
        this.enrichParties(Object.values(grouped).flat());
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
        this.enrichParties(bills);
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
        this.enrichParties(bills);
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
        this.enrichParties(bills);
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
    return this.applyPartyFilterAndSort(this.groupedBills()[typeCode] ?? []);
  }

  protected isExhausted(typeCode: string): boolean {
    return this.exhaustedTypes().has(typeCode);
  }
}
