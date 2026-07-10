import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ParliamentaryActivity, TagSeverity, VoteRecord } from '../../../../core/models';
import { UiSection } from '../../../../shared/ui/ui-section/ui-section';
import { UiStat } from '../../../../shared/ui/ui-stat/ui-stat';
import { UiTag } from '../../../../shared/ui/ui-tag/ui-tag';
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
  imports: [UiSection, UiStat, UiTag, UiIcon, LegislativeList, TranslatePipe],
  templateUrl: './profile-parliamentary.html',
  styleUrl: './profile-parliamentary.scss',
})
export class ProfileParliamentary {
  readonly activity = input.required<ParliamentaryActivity>();

  protected voteMeta(vote: VoteRecord['vote']) {
    return VOTE_META[vote];
  }
}
