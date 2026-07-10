import { ChangeDetectionStrategy, Component, computed, inject, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SelectModule } from 'primeng/select';
import { FeedSort as FeedSortValue } from '../../../../core/models';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { TranslateService } from '../../../../core/services/translate.service';

interface SortOption {
  label: string;
  value: FeedSortValue;
}

/** Section header + PrimeNG sort selector for the feed. */
@Component({
  selector: 'app-feed-sort',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, SelectModule, TranslatePipe],
  template: `
    <div class="feed-sort">
      <h2 class="feed-sort__title">{{ titleKey() | translate: title() }}</h2>
      <div class="feed-sort__control">
        <span class="feed-sort__label">{{ 'label.sort-by' | translate: 'Sort by:' }}</span>
        <p-select
          [options]="options()"
          [ngModel]="value()"
          (ngModelChange)="sortChange.emit($event)"
          optionLabel="label"
          optionValue="value"
          appendTo="body"
          styleClass="feed-sort__select"
        />
      </div>
    </div>
  `,
  styles: `
    .feed-sort {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 var(--cp-space-xs);
      gap: var(--cp-space-md);
    }
    .feed-sort__title {
      margin: 0;
      font-size: 20px;
      line-height: 28px;
      font-weight: 600;
      color: var(--cp-on-surface);
    }
    .feed-sort__control {
      display: flex;
      align-items: center;
      gap: var(--cp-space-sm);
    }
    .feed-sort__label {
      font-size: 14px;
      color: var(--cp-on-surface-variant);
      white-space: nowrap;
    }
  `,
})
export class FeedSort {
  private readonly translate = inject(TranslateService);

  readonly title = input('Recent Activity');
  readonly titleKey = input('section.recent-activity');
  readonly value = input.required<FeedSortValue>();
  readonly sortChange = output<FeedSortValue>();

  protected readonly options = computed<SortOption[]>(() => [
    { label: this.translate.t('label.sort-top', 'Top'), value: 'top' },
    { label: this.translate.t('label.sort-latest', 'Latest'), value: 'latest' },
    { label: this.translate.t('label.sort-following', 'Following'), value: 'following' },
  ]);
}
