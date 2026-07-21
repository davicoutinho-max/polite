import { inject } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { CanMatchFn, Router } from '@angular/router';
import { filter, map, take } from 'rxjs';
import { Permission } from '../models';
import { SessionService } from '../services/session.service';

/**
 * Route guard factory: only matches the route when the current account holds
 * `permission`. Otherwise it redirects to the public feed, so typing a
 * restricted URL directly is blocked exactly like the hidden nav item.
 *
 * Waits for `session.ready()` first — on a hard refresh the account starts out as the
 * placeholder visitor until the token-restore call resolves; evaluating `can()` before that
 * settles would incorrectly bounce an actually-signed-in user to /feed.
 */
export function requirePermission(permission: Permission): CanMatchFn {
  return () => {
    const session = inject(SessionService);
    const router = inject(Router);
    return toObservable(session.ready).pipe(
      filter((ready) => ready),
      take(1),
      map(() => (session.can(permission) ? true : router.parseUrl('/feed'))),
    );
  };
}
