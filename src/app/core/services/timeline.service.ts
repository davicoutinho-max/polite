import { computed, Injectable, signal } from '@angular/core';
import { TimelineEvent, TimelineEventType } from '../models';

interface EventVisual {
  readonly icon: string;
  readonly severity: 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'info' | 'neutral';
}

/** Icon + colour language for each timeline event type. */
export const TIMELINE_VISUALS: Record<TimelineEventType, EventVisual> = {
  vote: { icon: 'how_to_vote', severity: 'success' },
  project: { icon: 'description', severity: 'secondary' },
  committee: { icon: 'gavel', severity: 'primary' },
  video: { icon: 'smart_display', severity: 'info' },
  event: { icon: 'event', severity: 'secondary' },
  honor: { icon: 'military_tech', severity: 'warning' },
  'party-change': { icon: 'sync_alt', severity: 'warning' },
  campaign: { icon: 'campaign', severity: 'primary' },
  accounts: { icon: 'receipt_long', severity: 'success' },
};

@Injectable({ providedIn: 'root' })
export class TimelineService {
  private readonly _events = signal<TimelineEvent[]>([
    { id: 'e1', type: 'vote', title: 'Voted YES on PEC 33 — Fiscal Transparency', detail: 'Constitution & Justice Committee', timeLabel: '09:20', group: 'Today', actor: 'Jane Doe' },
    { id: 'e2', type: 'committee', title: 'Attended the Finance & Taxation Committee', timeLabel: '11:00', group: 'Today', actor: 'Jane Doe' },
    { id: 'e3', type: 'video', title: 'Published a video on the water bill', detail: '2.3k views', timeLabel: '14:45', group: 'Today', actor: 'Jane Doe' },
    { id: 'e4', type: 'project', title: 'Filed a new bill: Open Municipal Budget Ledger', timeLabel: '16:10', group: 'Today', actor: 'Jane Doe' },
    { id: 'e5', type: 'event', title: 'Joined the Open Government public hearing', detail: 'City Hall', timeLabel: 'Yesterday', group: 'Yesterday', actor: 'Jane Doe' },
    { id: 'e6', type: 'honor', title: 'Received a civic transparency award', timeLabel: 'Yesterday', group: 'Yesterday', actor: 'Jane Doe' },
    { id: 'e7', type: 'accounts', title: 'Filed quarterly expense accounts', detail: 'Q2 2026', timeLabel: 'Mon', group: 'This week', actor: 'Jane Doe' },
    { id: 'e8', type: 'campaign', title: 'Launched the "Clean Water Now" campaign', timeLabel: 'Tue', group: 'This week', actor: 'Jane Doe' },
    { id: 'e9', type: 'party-change', title: 'Marcus Chen changed party affiliation', detail: 'Now Independent', timeLabel: 'Wed', group: 'This week', actor: 'Marcus Chen' },
  ]);

  readonly events = this._events.asReadonly();

  /** Events grouped by their bucket, preserving order. */
  readonly grouped = computed(() => {
    const groups = new Map<string, TimelineEvent[]>();
    for (const event of this._events()) {
      const bucket = groups.get(event.group) ?? [];
      bucket.push(event);
      groups.set(event.group, bucket);
    }
    return [...groups.entries()].map(([group, events]) => ({ group, events }));
  });
}
