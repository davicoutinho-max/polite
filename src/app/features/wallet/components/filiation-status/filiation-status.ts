import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { FiliationStatus, FiliationStep } from '../../../../core/models';
import { UiButton } from '../../../../shared/ui/ui-button/ui-button';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { UiSection } from '../../../../shared/ui/ui-section/ui-section';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

/**
 * Official affiliation flow visualiser. The confirmation genuinely depends on
 * the party and the Electoral Justice — the app only orchestrates the request.
 */
@Component({
  selector: 'app-filiation-status',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiSection, UiIcon, UiButton, TranslatePipe],
  templateUrl: './filiation-status.html',
  styleUrl: './filiation-status.scss',
})
export class FiliationStatusComponent {
  readonly steps = input.required<readonly FiliationStep[]>();
  readonly status = input.required<FiliationStatus>();
  /** Index of the current (last completed) step; -1 when not started. */
  readonly currentIndex = input.required<number>();

  readonly request = output<void>();
  readonly advance = output<void>();
  readonly reset = output<void>();

  protected stepState(index: number): 'done' | 'active' | 'pending' {
    if (index < this.currentIndex()) {
      return 'done';
    }
    if (index === this.currentIndex()) {
      return 'active';
    }
    return 'pending';
  }
}
