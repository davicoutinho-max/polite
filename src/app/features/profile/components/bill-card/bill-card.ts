import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Bill } from '../../../../core/models';
import { UiCard } from '../../../../shared/ui/ui-card/ui-card';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { UiIconButton } from '../../../../shared/ui/ui-icon-button/ui-icon-button';
import { UiProgress } from '../../../../shared/ui/ui-progress/ui-progress';
import { UiTag } from '../../../../shared/ui/ui-tag/ui-tag';

/** Rich legislation card used on the politician profile. */
@Component({
  selector: 'app-bill-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiCard, UiTag, UiIcon, UiIconButton, UiProgress],
  templateUrl: './bill-card.html',
  styleUrl: './bill-card.scss',
})
export class BillCard {
  readonly bill = input.required<Bill>();
}
