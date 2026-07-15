import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Bill } from '../models';
import { DirectoryService } from './directory.service';
import { STATUS_TAGS } from './politician.service';

interface LegislativeItemResponseDto {
  readonly id: string;
  readonly politicianAccountId: string;
  readonly reference: string;
  readonly title: string;
  readonly summary: string | null;
  readonly category: string;
  readonly status: string;
}

/** Bills shown in the feed sidebar, sourced from legislative-service's global recent-items feed. */
@Injectable({ providedIn: 'root' })
export class BillsService {
  private readonly http = inject(HttpClient);
  private readonly directory = inject(DirectoryService);
  private readonly apiBase = `${environment.apiBaseUrl}/api/legislative`;

  private readonly _relevantBills = signal<Bill[]>([]);
  readonly relevantBills = this._relevantBills.asReadonly();

  constructor() {
    this.reloadRelevantBills().subscribe();
  }

  reloadRelevantBills(limit = 5): Observable<Bill[]> {
    return this.http.get<LegislativeItemResponseDto[]>(`${this.apiBase}/legislative-items`, { params: { recent: true, limit } }).pipe(
      map((items) =>
        items.map((i): Bill => {
          const sponsor = this.directory.politicians().find((p) => p.id === i.politicianAccountId);
          return {
            id: i.id,
            reference: i.reference,
            title: i.title,
            summary: i.summary ?? undefined,
            sponsor: sponsor ? `Sponsored by ${sponsor.name}` : undefined,
            politicianId: i.politicianAccountId,
            status: STATUS_TAGS[i.status] ?? { label: i.status, severity: 'neutral' as const },
          };
        }),
      ),
      tap((bills) => this._relevantBills.set(bills)),
    );
  }
}
