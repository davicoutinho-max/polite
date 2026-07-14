import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { SessionService } from '../../core/services/session.service';
import { UiButton } from '../../shared/ui/ui-button/ui-button';
import { UiIcon } from '../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { TranslateService } from '../../core/services/translate.service';
import { isValidCpf } from '../../shared/utils/br-documents';

/** Derives a handle from the display name (identity-service requires one, but this form only
 * collects a name) — lowercased, non-alphanumeric stripped, with a short random suffix to avoid
 * collisions since two people can share a name. */
function deriveHandle(name: string): string {
  const slug = name
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[^a-z0-9]+/g, '')
    .slice(0, 20);
  const suffix = Math.floor(Math.random() * 10000);
  return `${slug || 'user'}${suffix}`;
}

/**
 * Sign-up screen — a real citizen registration against identity-service.
 * Politicians are no longer self-service — a party registers them directly
 * from Party Admin (see admin-page.ts), and party accounts are created by a
 * platform admin, so citizen is the only self-registration path.
 * CPF is mandatory here too: CivicPulse ties every individual account
 * (citizen or politician) to a verified taxpayer ID.
 */
@Component({
  selector: 'app-register-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, UiButton, UiIcon, TranslatePipe],
  templateUrl: './register-page.html',
  styleUrl: './auth-page.scss',
})
export class RegisterPage {
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);

  protected readonly name = signal('');
  protected readonly email = signal('');
  protected readonly cpf = signal('');
  protected readonly password = signal('');
  protected readonly error = signal('');
  protected readonly submitting = signal(false);

  protected submit(): void {
    if (!this.name().trim() || !this.email().trim() || !this.cpf().trim() || !this.password().trim()) {
      this.error.set(
        this.translate.t(
          'error.fill-name-email-cpf-password',
          'Fill in your name, email, CPF and password to create an account.',
        ),
      );
      return;
    }
    if (!isValidCpf(this.cpf())) {
      this.error.set(this.translate.t('error.invalid-cpf', 'Enter a valid CPF (11 digits).'));
      return;
    }

    this.error.set('');
    this.submitting.set(true);
    const handle = deriveHandle(this.name().trim());
    this.session.register(this.name().trim(), handle, this.email().trim(), this.password().trim(), this.cpf().replace(/\D/g, '')).subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigateByUrl('/feed');
      },
      error: () => {
        this.submitting.set(false);
        this.error.set(this.translate.t('error.registration-failed', 'Could not create your account. That email or CPF may already be registered.'));
      },
    });
  }
}
