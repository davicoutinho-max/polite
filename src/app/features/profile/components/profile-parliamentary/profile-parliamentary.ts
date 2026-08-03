import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { ParliamentaryActivity, TagSeverity, VoteRecord } from '../../../../core/models';
import { UiSection } from '../../../../shared/ui/ui-section/ui-section';
import { StatStripItem, UiStatStrip } from '../../../../shared/ui/ui-stat-strip/ui-stat-strip';
import { UiTag } from '../../../../shared/ui/ui-tag/ui-tag';
import { TranslateService } from '../../../../core/services/translate.service';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { LegislativeList } from '../legislative-list/legislative-list';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

const VOTE_META: Record<VoteRecord['vote'], { label: string; key: string; severity: TagSeverity }> = {
  yes: { label: 'YES', key: 'vote.yes', severity: 'success' },
  no: { label: 'NO', key: 'vote.no', severity: 'danger' },
  abstain: { label: 'ABSTAIN', key: 'vote.abstain', severity: 'warning' },
  absent: { label: 'ABSENT', key: 'vote.absent', severity: 'neutral' },
};

/** Parliamentary activity tab: KPIs, committees, legislative production, votes. */
@Component({
  selector: 'app-profile-parliamentary',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiSection, UiStatStrip, UiTag, UiIcon, LegislativeList, TranslatePipe],
  templateUrl: './profile-parliamentary.html',
  styleUrl: './profile-parliamentary.scss',
})
export class ProfileParliamentary {
  private readonly translate = inject(TranslateService);

  readonly activity = input.required<ParliamentaryActivity>();

  protected readonly statItems = computed<StatStripItem[]>(() => {
    const a = this.activity();
    const t = (key: string, fallback: string) => this.translate.t(key, fallback);
    return [
      {
        icon: 'event_available',
        label: t('label.attendance', 'Attendance'),
        value: `${a.attendance.presenceRate}%`,
        caption: `${a.attendance.present} ${t('label.sessions-present', 'sessions present')}`,
      },
      {
        icon: 'event_busy',
        label: t('label.absences', 'Absences'),
        value: a.attendance.absent.toString(),
        caption: t('label.recorded-absences', 'Recorded absences'),
      },
      {
        icon: 'record_voice_over',
        label: t('label.speeches', 'Speeches'),
        value: a.speeches.toString(),
        caption: t('label.floor-speeches', 'Floor speeches'),
      },
      {
        icon: 'mic',
        label: t('label.interviews', 'Interviews'),
        value: a.interviews.toString(),
        caption: t('label.press-media', 'Press & media'),
      },
      {
        icon: 'flight_takeoff',
        label: t('label.official-trips', 'Official trips'),
        value: a.trips.toString(),
        caption: t('label.this-term', 'This term'),
      },
      {
        icon: 'how_to_vote',
        label: t('label.votes', 'Votes'),
        value: a.votes.length.toString(),
        caption: t('label.recent-records', 'Recent records'),
      },
    ];
  });

  protected voteMeta(vote: VoteRecord['vote']) {
    return VOTE_META[vote];
  }
}
