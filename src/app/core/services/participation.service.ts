import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Consultation,
  ConsultationStance,
  Petition,
  PetitionSignatureVerificationStarted,
  PetitionType,
  PollOption,
  StartPetitionSignatureCommand,
  Survey,
} from '../models';
import { SessionService } from './session.service';
import { TranslateService } from './translate.service';

interface PetitionResponseDto {
  readonly id: string;
  readonly title: string;
  readonly summary: string | null;
  readonly category: string | null;
  readonly goal: number;
  readonly signaturesCount: number;
  readonly deadline: string | null;
  readonly imageUrl: string | null;
  readonly videoUrl: string | null;
  readonly fileUrl: string | null;
  readonly fileName: string | null;
  readonly petitionType: PetitionType;
}

interface MediaUploadResponseDto {
  readonly url: string;
  readonly fileName: string;
}

interface StartSignatureResponseDto {
  readonly verificationId: string;
  readonly demoCode: string;
  readonly contact: string | null;
  readonly method: string;
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

function formatDeadline(translate: TranslateService, iso: string | null, prefixKey: string, prefixFallback: string): string {
  if (!iso) {
    return translate.t('label.open-ended', 'Open-ended');
  }
  const date = new Date(`${iso}T00:00:00`);
  const formatted = Number.isNaN(date.getTime()) ? iso : date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  return `${translate.t(prefixKey, prefixFallback)} ${formatted}`;
}

function petitionStatus(translate: TranslateService, signaturesCount: number, goal: number): Petition['status'] {
  if (goal > 0 && signaturesCount >= goal) {
    return { label: translate.t('label.goal-reached', 'Goal reached'), severity: 'secondary' };
  }
  if (goal > 0 && signaturesCount / goal >= 0.8) {
    return { label: translate.t('label.almost-there', 'Almost there'), severity: 'warning' };
  }
  return { label: translate.t('label.open', 'Open'), severity: 'success' };
}

@Injectable({ providedIn: 'root' })
export class ParticipationService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly translate = inject(TranslateService);
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

  uploadPetitionMedia(file: File): Observable<{ url: string; fileName: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<MediaUploadResponseDto>(`${environment.apiBaseUrl}/api/feed/media`, formData);
  }

  /** Step 1 of the DocuSign-like sign flow: captures the tier-specific identity fields and
   * returns a verification code (stood in for a real SMS/email send — see `demoCode`). Nothing is
   * recorded as a signature yet. */
  startPetitionSignature(petitionId: string, command: StartPetitionSignatureCommand): Observable<PetitionSignatureVerificationStarted> {
    const citizenId = this.citizenId;
    if (!citizenId) {
      throw new Error('Sign in to sign a petition');
    }
    return this.http
      .post<StartSignatureResponseDto>(`${this.apiBase}/petitions/${petitionId}/signatures/start`, {
        citizenAccountId: citizenId,
        ...command,
      })
      .pipe(map((dto) => ({ verificationId: dto.verificationId, demoCode: dto.demoCode, contact: dto.contact, method: dto.method })));
  }

  /** Step 2: confirms the code and, only then, materializes the real signature. */
  confirmPetitionSignature(petitionId: string, verificationId: string, code: string): Observable<void> {
    const citizenId = this.citizenId;
    if (!citizenId) {
      throw new Error('Sign in to sign a petition');
    }
    return this.http
      .post<void>(`${this.apiBase}/petitions/${petitionId}/signatures/confirm`, { citizenAccountId: citizenId, verificationId, code })
      .pipe(
        tap(() =>
          this._petitions.update((list) =>
            list.map((p) => (p.id === petitionId && !p.signed ? { ...p, signed: true, signatures: p.signatures + 1 } : p)),
          ),
        ),
      );
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

  createSurvey(question: string, context: string, options: string[]): Observable<Survey> {
    return this.http
      .post<SurveyResponseDto>(`${this.apiBase}/surveys`, { question, context, options })
      .pipe(
        switchMap((dto) => this.toSurvey(dto)),
        tap((survey) => this._surveys.update((list) => [survey, ...list])),
      );
  }

  /** Only politicians/parties reach the composer that calls this (see participation-page's
   * `canCreate` gate) — `deadline` is an optional ISO date (yyyy-MM-dd); omitting it leaves the
   * petition open-ended. Attachment URLs come from `uploadPetitionMedia()` having already run. */
  createPetition(
    title: string,
    summary: string,
    category: string,
    goal: number,
    deadline: string | null,
    petitionType: PetitionType,
    imageUrl: string | null,
    videoUrl: string | null,
    fileUrl: string | null,
    fileName: string | null,
  ): Observable<Petition> {
    return this.http
      .post<PetitionResponseDto>(`${this.apiBase}/petitions`, {
        title,
        summary,
        category,
        goal,
        deadline,
        petitionType,
        imageUrl,
        videoUrl,
        fileUrl,
        fileName,
      })
      .pipe(
        switchMap((dto) => this.toPetition(dto)),
        tap((petition) => this._petitions.update((list) => [petition, ...list])),
      );
  }

  createConsultation(title: string, description: string, deadline: string | null): Observable<Consultation> {
    return this.http
      .post<ConsultationResponseDto>(`${this.apiBase}/consultations`, { title, description, deadline })
      .pipe(
        switchMap((dto) => this.toConsultation(dto)),
        tap((consultation) => this._consultations.update((list) => [consultation, ...list])),
      );
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
          deadline: formatDeadline(this.translate, dto.deadline, 'label.closes', 'Closes'),
          status: petitionStatus(this.translate, dto.signaturesCount, dto.goal),
          signed,
          imageUrl: dto.imageUrl,
          videoUrl: dto.videoUrl,
          fileUrl: dto.fileUrl,
          fileName: dto.fileName,
          petitionType: dto.petitionType,
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
          deadline: formatDeadline(this.translate, dto.deadline, 'label.open-until', 'Open until'),
          status: { label: this.translate.t('label.open', 'Open'), severity: 'success' },
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
