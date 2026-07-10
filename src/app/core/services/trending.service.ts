import { Injectable, signal } from '@angular/core';
import { TrendingTopic } from '../models';

@Injectable({ providedIn: 'root' })
export class TrendingService {
  readonly topics = signal<TrendingTopic[]>([
    { id: 't1', rank: 1, category: 'National Policy', title: 'Infrastructure Bill', postCount: '45.2K posts' },
    { id: 't2', rank: 2, category: 'Local Governance', title: 'City Council Zoning', postCount: '12.8K posts' },
    { id: 't3', rank: 3, category: 'Environment', title: 'Clean Energy Subsidy', postCount: '8.9K posts' },
  ]).asReadonly();
}
