import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AnalyticsService } from '../../core/services/analytics.service';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiStat } from '../../shared/ui/ui-stat/ui-stat';
import { UiSection } from '../../shared/ui/ui-section/ui-section';
import { UiBarChart } from '../../shared/ui/ui-bar-chart/ui-bar-chart';
import { UiAreaChart } from '../../shared/ui/ui-area-chart/ui-area-chart';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Analytics dashboard: KPIs and single-hue charts. */
@Component({
  selector: 'app-analytics-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeader, UiStat, UiSection, UiBarChart, UiAreaChart, TranslatePipe],
  templateUrl: './analytics-page.html',
  styleUrl: './analytics-page.scss',
})
export class AnalyticsPage {
  private readonly analytics = inject(AnalyticsService);

  protected readonly kpis = this.analytics.kpis;
  protected readonly engagement = this.analytics.engagement;
  protected readonly byContentType = this.analytics.byContentType;
  protected readonly byAccountType = this.analytics.byAccountType;
}
