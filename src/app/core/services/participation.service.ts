import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Consultation, ConsultationStance, Petition, PollOption, Survey } from '../models';
import { SessionService } from './session.service';

interface PetitionResponseDto {
  readonly id: string;
  readonly title: string;
  readonly summary: string | null;
  readonly category: string | null;
  readonly goal: number;
  readonly signaturesCount: number;
  readonly deadline: string | null;
}

interface ConsultationResponseDto {
  readonly id: string;
  readonly title: string;
  readonly description: string | null;
  readonly deadline: string | null;
  readonly responsesCount: number;
}

interface SurveyResponseDto {
  readonly id: string;
  readonly question: string;
  readonly context: string | null;
}

interface SurveyOptionResponseDto {
  readonly id: string;
  readonly label: string;
  readonly votesCount: number;
}

function formatDeadline(iso: string | null, prefix: string): string {
  if (!iso) {
    return 'Open-ended';
  }
  const date = new Date(`${iso}T00:00:00`);
  const formatted = Number.isNaN(date.getTime()) ? iso : date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  return `${prefix} ${formatted}`;
}

function petitionStatus(signaturesCount: number, goal: number): Petition['status'] {
  if (goal > 0 && signaturesCount >= goal) {
    return { label: 'Goal reached', severity: 'secondary' };
  }
  if (goal > 0 && signaturesCount / goal >= 0.8) {
    return { label: 'Almost there', severity: 'warning' };
  }
  return { label: 'Open', severity: 'success' };
}

@Injectable({ providedIn: 'root' })
export class ParticipationService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly apiBase = `${environment.apiBaseUrl}/api/participation`;

  private readonly _petitions = signal<Petition[]>([]);
  readonly petitions = this._petitions.asReadonly();

  private readonly _consultations = signal<Consultation[]>([]);
  readonly consultations = this._consultations.asReadonly();

  private readonly _surveys = signal<Survey[]>([]);
  readonly surveys = this._surveys.asReadonly();

  constructor() {
    this.reloadPetitions().subscribe();
    this.reloadConsultations().subscribe();
    this.reloadSurveys().subscribe();
  }

  private get citizenId(): string | null {
    return this.session.isAuthenticated() ? this.session.account().id : null;
  }

  reloadPetitions(page = 0, pageSize = 50): Observable<Petition[]> {
    return this.http.get<PetitionResponseDto[]>(`${this.apiBase}/petitions`, { params: { page, pageSize } }).pipe(
      switchMap((list) => (list.length ? forkJoin(list.map((dto) => this.toPetition(dto))) : of([]))),
      tap((petitions) => this._petitions.set(petitions)),
    );
  }

  reloadConsultations(page = 0, pageSize = 50): Observable<Consultation[]> {
    return this.http.get<ConsultationResponseDto[]>(`${this.apiBase}/consultations`, { params: { page, pageSize } }).pipe(
      switchMap((list) => (list.length ? forkJoin(list.map((dto) => this.toConsultation(dto))) : of([]))),
      tap((consultations) => this._consultations.set(consultations)),
    );
  }

  reloadSurveys(page = 0, pageSize = 50): Observable<Survey[]> {
    return this.http.get<SurveyResponseDto[]>(`${this.apiBase}/surveys`, { params: { page, pageSize } }).pipe(
      switchMap((list) => (list.length ? forkJoin(list.map((dto) => this.toSurvey(dto))) : of([]))),
      tap((surveys) => this._surveys.set(surveys)),
    );
  }

  signPetition(id: string): void {
    const citizenId = this.citizenId;
    if (!citizenId) {
      return;
    }
    this.http.post(`${this.apiBase}/petitions/${id}/signatures`, { citizenAccountId: citizenId }).subscribe({
      next: () =>
        this._petitions.update((list) =>
          list.map((p) => (p.id === id && !p.signed ? { ...p, signed: true, signatures: p.signatures + 1 } : p)),
        ),
    });
  }

  setStance(id: string, stance: ConsultationStance): void {
    const citizenId = this.citizenId;
    if (!citizenId) {
      return;
    }
    this.http.post(`${this.apiBase}/consultations/${id}/responses`, { citizenAccountId: citizenId, stance }).subscribe({
      next: () =>
        this._consultations.update((list) =>
          list.map((c) => (c.id === id ? { ...c, stance, responses: c.stance === null ? c.responses + 1 : c.responses } : c)),
        ),
    });
  }

  vote(surveyId: string, optionId: string): void {
    const citizenId = this.citizenId;
    if (!citizenId) {
      return;
    }
    this.http.post(`${this.apiBase}/surveys/${surveyId}/votes`, { citizenAccountId: citizenId, optionId }).subscribe({
      next: () =>
        this._surveys.update((list) =>
          list.map((s) =>
            s.id === surveyId && s.votedOptionId === null
              ? { ...s, votedOptionId: optionId, options: s.options.map((o) => (o.id === optionId ? { ...o, votes: o.votes + 1 } : o)) }
              : s,
          ),
        ),
    });
  }

  private toPetition(dto: PetitionResponseDto): Observable<Petition> {
    const citizenId = this.citizenId;
    const signed$ = citizenId
      ? this.http.get<boolean>(`${this.apiBase}/petitions/${dto.id}/signatures/${citizenId}`).pipe(catchError(() => of(false)))
      : of(false);
    return signed$.pipe(
      map(
        (signed): Petition => ({
          id: dto.id,
          title: dto.title,
          summary: dto.summary ?? '',
          category: dto.category ?? '',
          goal: dto.goal,
          signatures: dto.signaturesCount,
          deadline: formatDeadline(dto.deadline, 'Closes'),
          status: petitionStatus(dto.signaturesCount, dto.goal),
          signed,
        }),
      ),
    );
  }

  private toConsultation(dto: ConsultationResponseDto): Observable<Consultation> {
    const citizenId = this.citizenId;
    const stance$: Observable<ConsultationStance | null> = citizenId
      ? this.http
          .get<string>(`${this.apiBase}/consultations/${dto.id}/responses/${citizenId}`)
          .pipe(
            map((s) => (s as ConsultationStance) ?? null),
            catchError(() => of(null)),
          )
      : of(null);
    return stance$.pipe(
      map(
        (stance): Consultation => ({
          id: dto.id,
          title: dto.title,
          description: dto.description ?? '',
          deadline: formatDeadline(dto.deadline, 'Open until'),
          status: { label: 'Open', severity: 'success' },
          responses: dto.responsesCount,
          stance,
        }),
      ),
    );
  }

  private toSurvey(dto: SurveyResponseDto): Observable<Survey> {
    const citizenId = this.citizenId;
    return forkJoin({
      options: this.http.get<SurveyOptionResponseDto[]>(`${this.apiBase}/surveys/${dto.id}/options`),
      votedOptionId: citizenId
        ? this.http.get<string>(`${this.apiBase}/surveys/${dto.id}/votes/${citizenId}`).pipe(
            map((id) => id ?? null),
            catchError(() => of(null)),
          )
        : of(null),
    }).pipe(
      map(
        ({ options, votedOptionId }): Survey => ({
          id: dto.id,
          question: dto.question,
          context: dto.context ?? '',
          options: options.map((o): PollOption => ({ id: o.id, label: o.label, votes: o.votesCount })),
          votedOptionId,
        }),
      ),
    );
  }
}
