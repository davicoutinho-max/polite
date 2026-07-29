import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

interface AskQuestionResponseDto {
  readonly answer: string;
}

/** Participation items a citizen can ask AI about — matches assistant-service's
 * AskParticipationQuestionService itemType handling. */
export type ParticipationItemType = 'petition' | 'consultation' | 'survey';

/** Real, live model calls (Gemini, via assistant-service) — see that service's
 * AskBillQuestionService/AskParticipationQuestionService for the system-instruction guardrails
 * that keep each call scoped to the one topic it's given, no matter what the citizen asks. */
@Injectable({ providedIn: 'root' })
export class AiAssistantService {
  private readonly http = inject(HttpClient);
  private readonly apiBase = `${environment.apiBaseUrl}/api/assistant`;

  askAboutBill(billIdentification: string, billSummary: string, question: string): Observable<string> {
    return this.http
      .post<AskQuestionResponseDto>(`${this.apiBase}/bills/ask`, { billIdentification, billSummary, question })
      .pipe(map((r) => r.answer));
  }

  askAboutParticipationItem(
    itemType: ParticipationItemType,
    title: string,
    description: string,
    question: string,
  ): Observable<string> {
    return this.http
      .post<AskQuestionResponseDto>(`${this.apiBase}/participation/ask`, { itemType, title, description, question })
      .pipe(map((r) => r.answer));
  }
}
