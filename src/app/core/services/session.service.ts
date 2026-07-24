import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, catchError, finalize, map, of, shareReplay, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { ACCOUNT_TYPE_OPTIONS, Account, AccountType, Permission, TYPE_PERMISSIONS, UserSummary } from '../models';

const ACCESS_TOKEN_KEY = 'cp_access_token';
const REFRESH_TOKEN_KEY = 'cp_refresh_token';

const GUEST_AVATAR =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 40 40'%3E%3Crect width='40' height='40' fill='%23c7ccd1'/%3E%3Ccircle cx='20' cy='15' r='7' fill='%23fff'/%3E%3Cpath d='M6 38c0-8 6-13 14-13s14 5 14 13z' fill='%23fff'/%3E%3C/svg%3E";

const VISITOR_ACCOUNT: Account = {
  id: 'guest',
  name: 'Visitor',
  handle: '@guest',
  role: 'Not signed in',
  accountType: 'visitor',
  avatarUrl: GUEST_AVATAR,
};

interface AuthResponse {
  readonly accountId: string;
  readonly accessToken: string;
  readonly accessTokenExpiresAt: string;
  readonly refreshToken: string;
}

interface AccountResponse {
  readonly id: string;
  readonly accountType: AccountType;
  readonly name: string;
  readonly handle: string;
  readonly email: string;
  readonly verified: boolean;
  readonly avatarUrl: string | null;
}

interface JwtPayload {
  readonly sub: string;
  readonly account_type: AccountType;
  readonly permissions?: readonly string[];
}

function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const [, payload] = token.split('.');
    const json = decodeURIComponent(
      atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join(''),
    );
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}

function accountTypeLabel(type: AccountType): string {
  return ACCOUNT_TYPE_OPTIONS.find((option) => option.type === type)?.label ?? type;
}

/**
 * Holds the current account and derives its permission set from the account type. Backed by a
 * real login against identity-service (see auth.interceptor.ts/error.interceptor.ts for how the
 * resulting token flows into every other HTTP call). `visitor` stays a pure client-side,
 * no-token state — it is never a real backend account.
 */
@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly http = inject(HttpClient);
  private readonly apiBase = `${environment.apiBaseUrl}/api/identity`;

  private readonly _account = signal<Account>(VISITOR_ACCOUNT);
  private readonly _accessToken = signal<string | null>(this.readStorage(ACCESS_TOKEN_KEY));
  private refreshTokenValue: string | null = this.readStorage(REFRESH_TOKEN_KEY);

  readonly account = this._account.asReadonly();
  /** Kept for backward compatibility with components reading a user summary. */
  readonly currentUser = computed<UserSummary>(() => this._account());

  readonly type = computed<AccountType>(() => this._account().accountType);
  readonly isVisitor = computed(() => this.type() === 'visitor');
  readonly isAuthenticated = computed(() => this.type() !== 'visitor');

  readonly permissions = computed<ReadonlySet<Permission>>(() => new Set<Permission>(TYPE_PERMISSIONS[this.type()]));

  /** Flips to true once the initial token-restore attempt below has fully settled (account
   * loaded, or confirmed absent/invalid) — permission.guard.ts waits on this so a hard refresh
   * on a permission-gated route can't evaluate `can()` against the placeholder visitor account
   * while the real one is still loading and get bounced to /feed by mistake. */
  private readonly _ready = signal(false);
  readonly ready = this._ready.asReadonly();

  constructor() {
    const token = this._accessToken();
    const payload = token ? decodeJwtPayload(token) : null;
    if (payload) {
      // Deferred to a microtask so this constructor has fully returned — and SessionService is
      // registered as a completed singleton — before the HTTP call reaches authInterceptor/
      // errorInterceptor, both of which `inject(SessionService)`. Firing it synchronously here
      // (the previous behavior) made Angular's DI see a request for SessionService while
      // SessionService's own constructor was still on the stack, which it correctly reports as a
      // circular dependency (NG0200) — that threw synchronously into this subscribe's `error`
      // callback below, which force-logged-out the user on every hard refresh before the restore
      // ever got a chance to run.
      queueMicrotask(() => {
        this.loadAccount(payload.sub).subscribe({
          next: () => this._ready.set(true),
          error: () => {
            this.forceLogout();
            this._ready.set(true);
          },
        });
      });
    } else {
      if (token) {
        this.forceLogout();
      }
      this._ready.set(true);
    }
  }

  can(permission: Permission): boolean {
    return this.permissions().has(permission);
  }

  accessToken(): string | null {
    return this._accessToken();
  }

  /** Real login against identity-service. Persists tokens, decodes the JWT for immediate
   * account-type/permissions, then fetches the account's display fields. */
  login(email: string, password: string): Observable<Account> {
    return this.http.post<AuthResponse>(`${this.apiBase}/auth/login`, { email, password }).pipe(
      tap((auth) => this.storeTokens(auth)),
      switchMap((auth) => this.loadAccount(auth.accountId)),
    );
  }

  /** Quick demo login — same real `login()` call, just against one of the seeded demo accounts
   * (see DEMO_CREDENTIALS). Preserves the old demo-switcher UX without faking the session. */
  /** Citizen self-registration, then an immediate real login (register itself doesn't return
   * tokens — see identity-service's AccountController). */
  register(name: string, handle: string, email: string, password: string, cpf: string): Observable<Account> {
    return this.http
      .post<AccountResponse>(`${this.apiBase}/accounts/register`, { name, handle, email, password, cpf })
      .pipe(switchMap(() => this.login(email, password)));
  }

  private refreshInFlight: Observable<boolean> | null = null;

  /** Attempts exactly one silent token refresh. Used by error.interceptor.ts on a 401 — never
   * called directly by feature code. The refresh token rotates server-side on every use (the old
   * one is revoked), so if several requests 401 around the same moment — the normal case, since
   * they all share one access-token expiry deadline — each call here must reuse a single in-flight
   * refresh rather than firing its own: a second concurrent call with the same (already-consumed)
   * refresh token would otherwise legitimately fail server-side and force-logout a session that the
   * first call had just successfully renewed. */
  refreshSession(): Observable<boolean> {
    if (this.refreshInFlight) {
      return this.refreshInFlight;
    }
    if (!this.refreshTokenValue) {
      return of(false);
    }
    this.refreshInFlight = this.http
      .post<AuthResponse>(`${this.apiBase}/auth/refresh`, { refreshToken: this.refreshTokenValue })
      .pipe(
        switchMap((auth) => {
          this.storeTokens(auth);
          return this.loadAccount(auth.accountId).pipe(map(() => true));
        }),
        catchError(() => of(false)),
        finalize(() => (this.refreshInFlight = null)),
        shareReplay(1),
      );
    return this.refreshInFlight;
  }

  /** Immediate, synchronous local sign-out — no network call. Used by the error interceptor when
   * a refresh itself fails, so the app never sits in a half-signed-in state. */
  forceLogout(): void {
    this.clearTokens();
    this._account.set(VISITOR_ACCOUNT);
  }

  /** User-initiated sign-out. Revokes the refresh token server-side (best-effort), then does a
   * hard browser navigation to /login rather than an in-SPA route change. Every other root
   * service (messages, wallet, notifications, feed, …) caches per-account state in signals that
   * are only ever populated once and never reset on logout — an in-SPA redirect would leave all
   * of that stale data on screen under the next signed-in identity. A full navigation tears down
   * the whole Angular injector and reloads fresh, which is the only way to guarantee none of that
   * lingers without auditing and patching every such service individually. */
  logout(): void {
    const refreshToken = this.refreshTokenValue;
    this.forceLogout();
    if (refreshToken) {
      this.http.post(`${this.apiBase}/auth/logout`, { refreshToken }).subscribe({ error: () => undefined });
    }
    window.location.href = '/login';
  }

  private loadAccount(accountId: string): Observable<Account> {
    return this.http.get<AccountResponse>(`${this.apiBase}/accounts/${accountId}`).pipe(
      map((response) => {
        const account: Account = {
          id: response.id,
          name: response.name,
          handle: `@${response.handle}`,
          role: accountTypeLabel(response.accountType),
          verified: response.verified,
          accountType: response.accountType,
          avatarUrl: response.avatarUrl ?? GUEST_AVATAR,
        };
        this._account.set(account);
        return account;
      }),
    );
  }

  private storeTokens(auth: AuthResponse): void {
    this._accessToken.set(auth.accessToken);
    this.refreshTokenValue = auth.refreshToken;
    localStorage.setItem(ACCESS_TOKEN_KEY, auth.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, auth.refreshToken);
  }

  private clearTokens(): void {
    this._accessToken.set(null);
    this.refreshTokenValue = null;
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }

  private readStorage(key: string): string | null {
    try {
      return localStorage.getItem(key);
    } catch {
      return null;
    }
  }
}
