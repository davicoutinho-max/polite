import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { InputText } from 'primeng/inputtext';
import { SessionService } from '../../core/services/session.service';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { TranslateService } from '../../core/services/translate.service';

/** Sign-in screen — a real login against identity-service. */
@Component({
  selector: 'app-login-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, UiButton, UiIcon, InputText, TranslatePipe],
  templateUrl: './login-page.html',
  styleUrl: './auth-page.scss',
})
export class LoginPage {
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);

  protected readonly email = signal('');
  protected readonly password = signal('');
  protected readonly error = signal('');
  protected readonly submitting = signal(false);

  protected submit(): void {
    if (!this.email().trim() || !this.password().trim()) {
      this.error.set(this.translate.t('error.enter-email-password', 'Enter your email and password to continue.'));
      return;
    }
    this.error.set('');
    this.submitting.set(true);
    this.session.login(this.email().trim(), this.password().trim()).subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigateByUrl('/feed');
      },
      error: () => {
        this.submitting.set(false);
        this.error.set(this.translate.t('error.invalid-credentials', 'Incorrect email or password.'));
      },
    });
  }
}
