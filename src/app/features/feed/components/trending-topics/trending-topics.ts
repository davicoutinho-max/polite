import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TrendingTopic } from '../../../../core/models';
import { UiSection } from '../../../../shared/ui/ui-section/ui-section';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

/** Sidebar widget listing trending civic topics. */
@Component({
  selector: 'app-trending-topics',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiSection, UiIcon, RouterLink, TranslatePipe],
  template: `
    <ui-section [title]="'section.trending-topics' | translate: 'Trending Topics'" icon="trending_up">
      <ul class="topics">
        @for (topic of topics(); track topic.id) {
          <li class="topic" routerLink="/feed">
            <span class="topic__rank">{{ topic.rank }}</span>
            <span class="topic__body">
              <span class="topic__category">{{ topic.category }}</span>
              <span class="topic__title">{{ topic.title }}</span>
              <span class="topic__count">
                <ui-icon name="forum" [size]="14" />
                {{ topic.postCount }}
              </span>
            </span>
            <ui-icon class="topic__chevron" name="chevron_right" [size]="20" />
          </li>
        }
      </ul>
    </ui-section>
  `,
  styles: `
    :host { display: block; }
    .topics { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: var(--cp-space-xs); }
    .topic {
      display: flex;
      align-items: center;
      gap: var(--cp-space-md);
      padding: var(--cp-space-sm);
      border-radius: var(--cp-radius-lg);
      cursor: pointer;
      transition: background 0.15s ease;
    }
    .topic:hover { background: var(--cp-surface-container-low); }
    .topic:hover .topic__chevron { color: var(--cp-secondary); transform: translateX(2px); }
    .topic:hover .topic__title { color: var(--cp-secondary); }

    .topic__rank {
      flex-shrink: 0;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 28px;
      border-radius: var(--cp-radius-full);
      background: var(--cp-secondary-container);
      color: var(--cp-on-secondary-container);
      font-size: 13px;
      font-weight: 700;
    }
    .topic__body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 1px; }
    .topic__category {
      font-size: 11px; font-weight: 600; letter-spacing: 0.05em; text-transform: uppercase;
      color: var(--cp-on-surface-variant);
    }
    .topic__title { font-size: 16px; font-weight: 700; color: var(--cp-on-surface); transition: color 0.15s ease; }
    .topic__count {
      display: inline-flex; align-items: center; gap: var(--cp-space-xs);
      font-size: 12px; color: var(--cp-on-surface-variant); margin-top: 2px;
    }
    .topic__chevron { color: var(--cp-outline); transition: transform 0.15s ease, color 0.15s ease; }
  `,
})
export class TrendingTopics {
  readonly topics = input.required<readonly TrendingTopic[]>();
}
