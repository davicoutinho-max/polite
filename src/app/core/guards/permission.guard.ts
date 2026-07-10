import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { Permission } from '../models';
import { SessionService } from '../services/session.service';

/**
 * Route guard factory: only matches the route when the current account holds
 * `permission`. Otherwise it redirects to the public feed, so typing a
 * restricted URL directly is blocked exactly like the hidden nav item.
 */
export function requirePermission(permission: Permission): CanMatchFn {
  return () => {
    const session = inject(SessionService);
    const router = inject(Router);
    return session.can(permission) ? true : router.parseUrl('/feed');
  };
}
