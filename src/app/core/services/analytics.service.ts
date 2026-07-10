import { Injectable, signal } from '@angular/core';
import { AreaPoint } from '../../shared/ui/ui-area-chart/ui-area-chart';
import { BarDatum } from '../../shared/ui/ui-bar-chart/ui-bar-chart';

export interface AnalyticsKpi {
  readonly icon: string;
  readonly label: string;
  readonly value: string;
  readonly caption: string;
}

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  readonly kpis = signal<AnalyticsKpi[]>([
    { icon: 'visibility', label: 'Reach', value: '284.6k', caption: '+12% vs last month' },
    { icon: 'group', label: 'Followers', value: '48.2k', caption: '+1,240 this week' },
    { icon: 'favorite', label: 'Engagement', value: '7.4%', caption: 'Avg. per post' },
    { icon: 'forum', label: 'Interactions', value: '19.8k', caption: 'Likes + comments' },
  ]).asReadonly();

  /** Weekly engagement rate (single series). */
  readonly engagement = signal<AreaPoint[]>([
    { label: 'Mon', value: 5.2 },
    { label: 'Tue', value: 6.1 },
    { label: 'Wed', value: 5.8 },
    { label: 'Thu', value: 7.9 },
    { label: 'Fri', value: 7.2 },
    { label: 'Sat', value: 8.6 },
    { label: 'Sun', value: 9.1 },
  ]).asReadonly();

  /** Interactions by content type (magnitude). */
  readonly byContentType = signal<BarDatum[]>([
    { label: 'Bills & projects', value: 8400, display: '8.4k' },
    { label: 'Videos', value: 6100, display: '6.1k' },
    { label: 'Transparency reports', value: 3200, display: '3.2k' },
    { label: 'Events', value: 1500, display: '1.5k' },
  ]).asReadonly();

  /** Reach by district (magnitude). */
  readonly byDistrict = signal<BarDatum[]>([
    { label: '4th District', value: 92000, display: '92k' },
    { label: '2nd District', value: 61000, display: '61k' },
    { label: '7th District', value: 44000, display: '44k' },
    { label: '1st District', value: 38000, display: '38k' },
    { label: 'Other', value: 49600, display: '49.6k' },
  ]).asReadonly();
}
