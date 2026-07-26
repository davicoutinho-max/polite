import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { SessionService } from '../services/session.service';

/** Attaches the bearer access token to every outgoing request to our own API gateway, except the
 * auth endpoints themselves (login/refresh/register never need — and logically can't need — a
 * token). Requests to third-party APIs (e.g. the federal legislature's open-data services) never
 * get our token — leaking it to an external domain would be a security bug, and some of those
 * APIs' CORS preflight doesn't allow-list the Authorization header anyway. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);

  if (!req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }

  if (req.url.includes('/auth/') || req.url.includes('/accounts/register')) {
    return next(req);
  }

  const token = session.accessToken();
  if (!token) {
    return next(req);
  }

  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
