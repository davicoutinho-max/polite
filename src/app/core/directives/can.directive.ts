import {
  Directive,
  effect,
  inject,
  input,
  TemplateRef,
  ViewContainerRef,
} from '@angular/core';
import { Permission } from '../models';
import { SessionService } from '../services/session.service';

/**
 * Structural directive that renders its content only when the current account
 * holds the given permission.
 *
 * @example
 * <ui-button *appCan="'vote-bill'" label="Vote" />
 */
@Directive({
  selector: '[appCan]',
})
export class CanDirective {
  private readonly session = inject(SessionService);
  private readonly template = inject(TemplateRef<unknown>);
  private readonly viewContainer = inject(ViewContainerRef);

  readonly appCan = input.required<Permission>();

  private visible = false;

  constructor() {
    effect(() => {
      const allowed = this.session.can(this.appCan());
      if (allowed && !this.visible) {
        this.viewContainer.createEmbeddedView(this.template);
        this.visible = true;
      } else if (!allowed && this.visible) {
        this.viewContainer.clear();
        this.visible = false;
      }
    });
  }
}
