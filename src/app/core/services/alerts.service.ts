import { computed, Injectable, signal } from '@angular/core';
import { Alert } from '../models';

@Injectable({ providedIn: 'root' })
export class AlertsService {
  private readonly _alerts = signal<Alert[]>([
    {
      id: 'a1',
      category: 'project',
      icon: 'description',
      title: 'New bill introduced',
      message: 'Jane Doe, who you follow, introduced PL 452/2024 — Clean Water Infrastructure Act.',
      timeLabel: '10 min ago',
      link: '/profile/jane-doe',
      read: false,
    },
    {
      id: 'a2',
      category: 'pec',
      icon: 'account_balance',
      title: 'New PEC',
      message: 'PEC 33/2024 — Fiscal Transparency Amendment reached the voting floor.',
      timeLabel: '1 hour ago',
      link: '/participation',
      read: false,
    },
    {
      id: 'a3',
      category: 'party',
      icon: 'groups',
      title: 'Party leadership changed',
      message: 'The Progressive Party elected a new president at the national convention.',
      timeLabel: '3 hours ago',
      read: false,
    },
    {
      id: 'a4',
      category: 'vote',
      icon: 'how_to_vote',
      title: 'New vote registered',
      message: 'A politician you follow voted YES on the Fiscal Transparency Amendment.',
      timeLabel: 'Yesterday',
      read: true,
    },
    {
      id: 'a5',
      category: 'cpi',
      icon: 'policy',
      title: 'New CPI',
      message: 'CPI 05/2023 — Public Contracts Inquiry opened a new investigation line.',
      timeLabel: '2 days ago',
      read: true,
    },
  ]);

  readonly alerts = this._alerts.asReadonly();
  readonly unreadCount = computed(() => this._alerts().filter((a) => !a.read).length);

  markRead(id: string): void {
    this._alerts.update((list) => list.map((a) => (a.id === id ? { ...a, read: true } : a)));
  }

  markAllRead(): void {
    this._alerts.update((list) => list.map((a) => ({ ...a, read: true })));
  }

  /** Emit a brand-new, unread notification at the top of the feed. */
  push(alert: Omit<Alert, 'id' | 'read'>): void {
    const entry: Alert = { ...alert, id: `a${Date.now()}`, read: false };
    this._alerts.update((list) => [entry, ...list]);
  }
}
