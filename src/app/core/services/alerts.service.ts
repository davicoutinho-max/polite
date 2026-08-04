import { HttpClient } from '@angular/common/http';
import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { Observable, map, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Alert, AlertCategory } from '../models';
import { relativeTime } from '../utils/relative-time';
import { SessionService } from './session.service';

interface NotificationResponseDto {
  readonly id: string;
  readonly category: string;
  readonly icon: string | null;
  readonly title: string;
  readonly message: string;
  readonly link: string | null;
  readonly read: boolean;
  readonly createdAt: string;
}

const TOAST_DURATION_MS = 5000;

/**
 * Server-driven alerts come from notification-service (real, persisted, read/unread tracked).
 * `push()` stays a purely local, ephemeral toast for the current user's own just-taken action
 * (e.g. "affiliation request sent") — it was never meant to be a persisted notification row.
 *
 * `push()` also surfaces a real floating toast (see `toasts`/ToastContainer, mounted once in
 * Shell) — previously it only ever landed silently in the bell-icon dropdown, which is easy to
 * miss as "did my action actually work?" feedback, especially for a save button at the bottom of
 * a long form while the bell sits up in the top bar out of view.
 */
@Injectable({ providedIn: 'root' })
export class AlertsService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly apiBase = `${environment.apiBaseUrl}/api/notifications`;

  private readonly _alerts = signal<Alert[]>([]);
  readonly alerts = this._alerts.asReadonly();
  readonly unreadCount = computed(() => this._alerts().filter((a) => !a.read).length);

  private readonly _toasts = signal<Alert[]>([]);
  readonly toasts = this._toasts.asReadonly();

  constructor() {
    // See DirectoryService's reloadFollowing for why this waits on session.ready() instead of
    // firing immediately — otherwise a hard refresh can permanently strand this at "no alerts".
    effect(() => {
      if (this.session.ready()) {
        this.reload().subscribe();
      }
    });
  }

  reload(page = 0, pageSize = 50): Observable<Alert[]> {
    if (!this.session.isAuthenticated()) {
      return of([]);
    }
    return this.http.get<NotificationResponseDto[]>(this.apiBase, { params: { page, pageSize } }).pipe(
      map((list) =>
        list.map(
          (dto): Alert => ({
            id: dto.id,
            category: dto.category as AlertCategory,
            icon: dto.icon ?? 'notifications',
            title: dto.title,
            message: dto.message,
            timeLabel: relativeTime(dto.createdAt),
            link: dto.link ?? undefined,
            read: dto.read,
          }),
        ),
      ),
      tap((alerts) => this._alerts.set(alerts)),
    );
  }

  markRead(id: string): void {
    this.http.post(`${this.apiBase}/${id}/read`, {}).subscribe({
      next: () => this._alerts.update((list) => list.map((a) => (a.id === id ? { ...a, read: true } : a))),
    });
  }

  markAllRead(): void {
    this.http.post(`${this.apiBase}/read-all`, {}).subscribe({
      next: () => this._alerts.update((list) => list.map((a) => ({ ...a, read: true }))),
    });
  }

  /** Emit a brand-new, unread LOCAL toast — lands at the top of the bell-icon dropdown feed
   * (not persisted server-side) and also floats on screen for a few seconds via ToastContainer. */
  push(alert: Omit<Alert, 'id' | 'read'>): void {
    const entry: Alert = { ...alert, id: `local-${Date.now()}-${Math.random().toString(36).slice(2)}`, read: false };
    this._alerts.update((list) => [entry, ...list]);
    this._toasts.update((list) => [...list, entry]);
    setTimeout(() => this.dismissToast(entry.id), TOAST_DURATION_MS);
  }

  dismissToast(id: string): void {
    this._toasts.update((list) => list.filter((t) => t.id !== id));
  }

  /** Broadcasts a real, persisted notification to every given recipient (e.g. "send
   * notification to all members" from a party admin panel). */
  broadcast(recipientAccountIds: string[], category: AlertCategory, icon: string, title: string, message: string, link?: string): Observable<void> {
    return this.http
      .post<void>(`${this.apiBase}/broadcast`, { recipientAccountIds, category, icon, title, message, link: link ?? null })
      .pipe(tap(() => undefined));
  }
}
