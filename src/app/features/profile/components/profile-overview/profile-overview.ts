import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { Politician } from '../../../../core/models';
import { UiAvatar } from '../../../../shared/ui/ui-avatar/ui-avatar';
import { DataListItem, UiDataList } from '../../../../shared/ui/ui-data-list/ui-data-list';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { UiSection } from '../../../../shared/ui/ui-section/ui-section';
import { UiTag } from '../../../../shared/ui/ui-tag/ui-tag';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { TranslateService } from '../../../../core/services/translate.service';

/** Overview tab: dossier, mandates, social links and team. */
@Component({
  selector: 'app-profile-overview',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiSection, UiDataList, UiAvatar, UiIcon, UiTag, TranslatePipe],
  templateUrl: './profile-overview.html',
  styleUrl: './profile-overview.scss',
})
export class ProfileOverview {
  readonly politician = input.required<Politician>();
  private readonly translate = inject(TranslateService);

  protected readonly dossier = computed<DataListItem[]>(() => {
    const p = this.politician();
    const t = (key: string, fallback: string) => this.translate.t(key, fallback);
    return [
      { icon: 'school', label: t('label.education', 'Education'), value: p.education },
      { icon: 'work', label: t('label.profession', 'Profession'), value: p.profession },
      { icon: 'account_balance', label: t('label.declared-patrimony', 'Declared patrimony'), value: p.patrimony },
      { icon: 'flag', label: t('label.party', 'Party'), value: p.party },
      { icon: 'mail', label: t('label.email', 'Email'), value: p.email },
      { icon: 'call', label: t('label.phone', 'Phone'), value: p.phone },
      { icon: 'location_on', label: t('label.office', 'Office'), value: p.office },
      { icon: 'event', label: t('label.serving-since', 'Serving since'), value: String(p.servingSince) },
    ];
  });
}
