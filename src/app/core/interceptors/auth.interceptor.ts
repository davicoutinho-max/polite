import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { SessionService } from '../services/session.service';

/** Attaches the bearer access token to every outgoing request except the auth endpoints
 * themselves (login/refresh/register never need — and logically can't need — a token). */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);

  if (req.url.includes('/auth/') || req.url.includes('/accounts/register')) {
    return next(req);
  }

  const token = session.accessToken();
  if (!token) {
    return next(req);
  }

  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
