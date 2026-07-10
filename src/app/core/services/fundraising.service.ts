import { computed, inject, Injectable, signal } from '@angular/core';
import {
  Fundraiser,
  FundraiserCategory,
  FundraiserCategoryMeta,
  NewFundraiser,
} from '../models';
import { AlertsService } from './alerts.service';
import { SessionService } from './session.service';

@Injectable({ providedIn: 'root' })
export class FundraisingService {
  private readonly alerts = inject(AlertsService);
  private readonly session = inject(SessionService);

  readonly categories: FundraiserCategoryMeta[] = [
    { category: 'social', label: 'Social cause', icon: 'diversity_3' },
    { category: 'party', label: 'Party initiative', icon: 'flag' },
    { category: 'humanitarian', label: 'Humanitarian aid', icon: 'volunteer_activism' },
  ];

  private readonly _fundraisers = signal<Fundraiser[]>([
    {
      id: 'fr1',
      title: 'Winter shelter supplies drive',
      description:
        'Blankets, warm meals and hygiene kits for families in the city shelter network during the cold season.',
      organizer: 'Progressive Party — Campinas directory',
      category: 'humanitarian',
      goal: 20000,
      raised: 13400,
      supporters: 312,
      deadline: 'Aug 15, 2026',
      ledgerPublic: true,
    },
    {
      id: 'fr2',
      title: 'Public school library renovation',
      description:
        'Restore and restock the community library at Escola Municipal Vila Nova, reaching 800 students.',
      organizer: 'Civic Education Collective',
      category: 'social',
      goal: 35000,
      raised: 28750,
      supporters: 540,
      deadline: 'Sep 1, 2026',
      ledgerPublic: true,
    },
    {
      id: 'fr3',
      title: 'Youth political education program',
      description:
        'Fund a series of free workshops on how laws are made and how to follow public spending.',
      organizer: 'Progressive Party — Youth wing',
      category: 'party',
      goal: 12000,
      raised: 4200,
      supporters: 96,
      deadline: 'Jul 30, 2026',
      ledgerPublic: false,
    },
  ]);

  readonly fundraisers = this._fundraisers.asReadonly();

  readonly totalRaised = computed(() =>
    this._fundraisers().reduce((sum, f) => sum + f.raised, 0),
  );
  readonly totalSupporters = computed(() =>
    this._fundraisers().reduce((sum, f) => sum + f.supporters, 0),
  );

  create(input: NewFundraiser): void {
    const fundraiser: Fundraiser = {
      id: `fr${Date.now()}`,
      title: input.title,
      description: input.description,
      organizer: this.session.account().name,
      category: input.category,
      goal: input.goal,
      raised: 0,
      supporters: 0,
      deadline: input.deadline,
      ledgerPublic: true,
    };
    this._fundraisers.update((list) => [fundraiser, ...list]);
    this.alerts.push({
      category: 'campaign',
      icon: 'volunteer_activism',
      title: 'Fundraising action created',
      message: `"${input.title}" is now live and accepting contributions.`,
      timeLabel: 'Just now',
      link: '/fundraising',
    });
  }

  contribute(id: string, amount: number): void {
    if (amount <= 0) {
      return;
    }
    this._fundraisers.update((list) =>
      list.map((f) =>
        f.id === id ? { ...f, raised: f.raised + amount, supporters: f.supporters + 1 } : f,
      ),
    );
  }

  categoryMeta(category: FundraiserCategory): FundraiserCategoryMeta {
    return this.categories.find((c) => c.category === category) ?? this.categories[0];
  }
}
