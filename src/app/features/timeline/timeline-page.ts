import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { TimelineService, TIMELINE_VISUALS } from '../../core/services/timeline.service';
import { TimelineEventType } from '../../core/models';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

/** Smart activity timeline grouped by time buckets (Today, Yesterday, …). */
@Component({
  selector: 'app-timeline-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeader, UiIcon, TranslatePipe],
  templateUrl: './timeline-page.html',
  styleUrl: './timeline-page.scss',
})
export class TimelinePage {
  private readonly timeline = inject(TimelineService);

  protected readonly groups = this.timeline.grouped;

  protected visual(type: TimelineEventType) {
    return TIMELINE_VISUALS[type];
  }
}
