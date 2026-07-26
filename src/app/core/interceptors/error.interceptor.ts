import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { SessionService } from '../services/session.service';

/** On a 401, tries exactly one silent refresh-then-retry; if the refresh itself fails (or there
 * was no refresh token to try), forces a logout instead of leaving the app in a half-signed-in
 * state. Never intercepts the auth endpoints themselves — a 401 from /auth/login is just a
 * wrong-password response, not an expired-session signal — nor third-party APIs (e.g. the
 * legislature's open-data services), which never carry our session token in the first place. */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);

  return next(req).pipe(
    catchError((error: unknown) => {
      const isOwnApi = req.url.startsWith(environment.apiBaseUrl);
      const isAuthEndpoint = req.url.includes('/auth/');
      if (!(error instanceof HttpErrorResponse) || error.status !== 401 || !isOwnApi || isAuthEndpoint) {
        return throwError(() => error);
      }

      return session.refreshSession().pipe(
        switchMap((refreshed) => {
          if (!refreshed) {
            session.forceLogout();
            return throwError(() => error);
          }
          const token = session.accessToken();
          const retried = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
          return next(retried);
        }),
        catchError((refreshError: unknown) => {
          session.forceLogout();
          return throwError(() => refreshError);
        }),
      );
    }),
  );
};
