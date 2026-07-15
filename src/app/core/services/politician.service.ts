import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, forkJoin, map, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CareerMilestone,
  Mandate,
  ParliamentaryActivity,
  Politician,
  ProfileTab,
  SocialLink,
  StatusTag,
  TeamMember,
  TransparencyReport,
} from '../models';
import { DirectoryService } from './directory.service';

const FALLBACK_AVATAR =
  'data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' viewBox=\'0 0 40 40\'%3E%3Crect width=\'40\' height=\'40\' fill=\'%23c7ccd1\'/%3E%3Ccircle cx=\'20\' cy=\'15\' r=\'7\' fill=\'%23fff\'/%3E%3Cpath d=\'M6 38c0-8 6-13 14-13s14 5 14 13z\' fill=\'%23fff\'/%3E%3C/svg%3E';

const PLATFORM_ICONS: Record<string, string> = {
  website: 'public',
  instagram: 'photo_camera',
  x: 'flutter_dash',
  facebook: 'thumb_up',
  youtube: 'smart_display',
  linkedin: 'work',
  tiktok: 'music_note',
};

export const STATUS_TAGS: Record<string, StatusTag> = {
  filed: { label: 'Filed', severity: 'neutral' },
  in_committee: { label: 'In Committee', severity: 'warning' },
  floor_vote: { label: 'Floor Vote', severity: 'info' },
  passed: { label: 'Approved', severity: 'success' },
  rejected: { label: 'Rejected', severity: 'danger' },
};

export const STATUS_PROGRESS: Record<string, number> = {
  filed: 25,
  in_committee: 50,
  floor_vote: 75,
  passed: 100,
  rejected: 100,
};

/** Formats an integer cents amount as a Brazilian-locale currency string, e.g. "R$ 41.650". */
export function formatCents(cents: number): string {
  return `R$ ${Math.round(cents / 100).toLocaleString('pt-BR')}`;
}

function firstYear(period: string): number | null {
  const match = period.match(/\d{4}/);
  return match ? Number(match[0]) : null;
}

const EMPTY_POLITICIAN: Politician = {
  id: '',
  name: '',
  handle: '',
  avatarUrl: FALLBACK_AVATAR,
  verified: false,
  party: 'Independent',
  partyId: '',
  position: '',
  servingSince: new Date().getFullYear(),
  coverUrl: FALLBACK_AVATAR,
  education: '',
  profession: '',
  patrimony: '',
  email: '',
  phone: '',
  office: '',
  mandates: [],
  socialLinks: [],
  team: [],
};

const EMPTY_ACTIVITY: ParliamentaryActivity = {
  projects: [],
  approvedLaws: [],
  rejectedLaws: [],
  pecs: [],
  cpis: [],
  committees: [],
  votes: [],
  attendance: { present: 0, absent: 0, presenceRate: 0 },
  speeches: 0,
  interviews: 0,
  trips: 0,
};

const EMPTY_TRANSPARENCY: TransparencyReport = {
  metrics: [],
  expenses: [],
  totalExpense: 'R$ 0',
  lastUpdate: '',
};

interface DossierResponseDto {
  readonly education: string | null;
  readonly profession: string | null;
  readonly patrimony: string | null;
  readonly email: string | null;
  readonly phone: string | null;
  readonly officeDetail: string | null;
  readonly speechesCount: number;
  readonly interviewsCount: number;
  readonly tripsCount: number;
}

interface MandateResponseDto {
  readonly role: string;
  readonly period: string;
  readonly current: boolean;
}

interface SocialLinkResponseDto {
  readonly platform: string;
  readonly label: string;
  readonly handle: string;
  readonly url: string;
}

interface TeamMemberResponseDto {
  readonly name: string;
  readonly role: string;
  readonly avatarUrl: string | null;
}

interface LegislativeItemResponseDto {
  readonly id: string;
  readonly reference: string;
  readonly title: string;
  readonly summary: string | null;
  readonly category: string;
  readonly status: string;
  readonly itemDate: string;
  readonly cosponsorAccountIds: string[];
}

interface CommitteeMembershipResponseDto {
  readonly name: string;
  readonly role: string;
  readonly kind: 'committee' | 'front' | 'cpi';
}

interface VoteRecordResponseDto {
  readonly id: string;
  readonly matter: string;
  readonly voteDate: string;
  readonly choice: 'yes' | 'no' | 'abstain' | 'absent';
}

interface AttendanceResponseDto {
  readonly present: number;
  readonly absent: number;
  readonly presenceRate: number;
}

interface TransparencyMetricResponseDto {
  readonly icon: string | null;
  readonly label: string;
  readonly valueCents: number;
  readonly caption: string | null;
  readonly period: string;
}

interface ExpenseLineResponseDto {
  readonly category: string;
  readonly amountCents: number;
  readonly sharePercent: number;
}

interface TransparencyResponseDto {
  readonly totalExpenseCents: number;
  readonly lastUpdate: string;
  readonly metrics: TransparencyMetricResponseDto[];
  readonly expenseLines: ExpenseLineResponseDto[];
}

interface CareerMilestoneResponseDto {
  readonly year: number;
  readonly title: string;
  readonly detail: string | null;
}

/**
 * A politician's full dossier: basic identity fields come from directory-service (already
 * loaded by DirectoryService), everything else — dossier, mandates, social links, team,
 * legislative activity, transparency, career — comes from legislative-service, joined
 * client-side per {@link load}.
 */
@Injectable({ providedIn: 'root' })
export class PoliticianService {
  private readonly http = inject(HttpClient);
  private readonly directory = inject(DirectoryService);
  private readonly apiBase = `${environment.apiBaseUrl}/api/legislative`;

  private readonly _politician = signal<Politician>(EMPTY_POLITICIAN);
  readonly politician = this._politician.asReadonly();

  private readonly _activity = signal<ParliamentaryActivity>(EMPTY_ACTIVITY);
  readonly activity = this._activity.asReadonly();

  private readonly _transparency = signal<TransparencyReport>(EMPTY_TRANSPARENCY);
  readonly transparency = this._transparency.asReadonly();

  private readonly _career = signal<CareerMilestone[]>([]);
  readonly career = this._career.asReadonly();

  readonly tabs = signal<ProfileTab[]>([
    { id: 'activity', label: 'Activity', key: 'tab.activity', icon: 'forum' },
    { id: 'overview', label: 'Overview', key: 'tab.overview', icon: 'badge' },
    { id: 'parliamentary', label: 'Parliamentary Activity', key: 'tab.parliamentary', icon: 'gavel' },
    { id: 'transparency', label: 'Transparency', key: 'tab.transparency', icon: 'analytics' },
    { id: 'timeline', label: 'Career Timeline', key: 'tab.career-timeline', icon: 'timeline' },
  ]).asReadonly();

  /** Loads everything shown on a politician's profile page for the given account id. */
  load(accountId: string): Observable<Politician> {
    return forkJoin({
      directoryEntry: this.directory.getPolitician(accountId),
      dossier: this.http.get<DossierResponseDto>(`${this.apiBase}/politicians/${accountId}/dossier`),
      mandates: this.http.get<MandateResponseDto[]>(`${this.apiBase}/politicians/${accountId}/mandates`),
      socialLinks: this.http.get<SocialLinkResponseDto[]>(`${this.apiBase}/politicians/${accountId}/social-links`),
      team: this.http.get<TeamMemberResponseDto[]>(`${this.apiBase}/politicians/${accountId}/team`),
    }).pipe(
      map(({ directoryEntry, dossier, mandates, socialLinks, team }): Politician => {
        const mappedMandates: Mandate[] = mandates.map((m) => ({ role: m.role, period: m.period, current: m.current }));
        const servingSince = mappedMandates
          .map((m) => firstYear(m.period))
          .filter((year): year is number => year !== null)
          .reduce((min, year) => Math.min(min, year), new Date().getFullYear());
        const party = this.directory.parties().find((p) => p.id === directoryEntry.partyId);

        return {
          id: directoryEntry.id,
          name: directoryEntry.name,
          handle: directoryEntry.handle,
          avatarUrl: directoryEntry.avatarUrl || FALLBACK_AVATAR,
          verified: directoryEntry.verified,
          role: directoryEntry.office,
          party: party?.name ?? directoryEntry.partyAcronym ?? 'Independent',
          partyId: directoryEntry.partyId || '',
          position: directoryEntry.office,
          servingSince,
          coverUrl: directoryEntry.avatarUrl || FALLBACK_AVATAR,
          education: dossier.education ?? '',
          profession: dossier.profession ?? '',
          patrimony: dossier.patrimony ?? '',
          email: dossier.email ?? '',
          phone: dossier.phone ?? '',
          office: dossier.officeDetail ?? '',
          mandates: mappedMandates,
          socialLinks: socialLinks.map(
            (s): SocialLink => ({ icon: PLATFORM_ICONS[s.platform] ?? 'link', label: s.label, handle: s.handle, url: s.url }),
          ),
          team: team.map((t): TeamMember => ({ name: t.name, role: t.role, avatarUrl: t.avatarUrl || FALLBACK_AVATAR })),
        };
      }),
      tap((politician) => this._politician.set(politician)),
    );
  }

  loadActivity(accountId: string): Observable<ParliamentaryActivity> {
    return forkJoin({
      items: this.http.get<LegislativeItemResponseDto[]>(`${this.apiBase}/legislative-items`, { params: { politicianAccountId: accountId } }),
      committees: this.http.get<CommitteeMembershipResponseDto[]>(`${this.apiBase}/politicians/${accountId}/committees`),
      votes: this.http.get<VoteRecordResponseDto[]>(`${this.apiBase}/politicians/${accountId}/votes`),
      attendance: this.http.get<AttendanceResponseDto>(`${this.apiBase}/politicians/${accountId}/attendance`),
      dossier: this.http.get<DossierResponseDto>(`${this.apiBase}/politicians/${accountId}/dossier`),
    }).pipe(
      map(({ items, committees, votes, attendance, dossier }): ParliamentaryActivity => {
        const toItem = (i: LegislativeItemResponseDto) => ({
          id: i.id,
          reference: i.reference,
          title: i.title,
          summary: i.summary ?? '',
          status: STATUS_TAGS[i.status] ?? { label: i.status, severity: 'neutral' as const },
          date: i.itemDate,
        });
        const projectItems = items.filter((i) => i.category === 'project');
        return {
          projects: projectItems.filter((i) => i.status !== 'passed' && i.status !== 'rejected').map(toItem),
          approvedLaws: projectItems.filter((i) => i.status === 'passed').map(toItem),
          rejectedLaws: items.filter((i) => i.status === 'rejected').map(toItem),
          pecs: items.filter((i) => i.category === 'pec').map(toItem),
          cpis: items.filter((i) => i.category === 'cpi').map(toItem),
          committees: committees.map((c) => ({ name: c.name, role: c.role, kind: c.kind })),
          votes: votes.map((v) => ({ id: v.id, matter: v.matter, date: v.voteDate, vote: v.choice })),
          attendance: { present: attendance.present, absent: attendance.absent, presenceRate: Math.round(attendance.presenceRate) },
          speeches: dossier.speechesCount,
          interviews: dossier.interviewsCount,
          trips: dossier.tripsCount,
        };
      }),
      tap((activity) => this._activity.set(activity)),
    );
  }

  loadTransparency(accountId: string): Observable<TransparencyReport> {
    return this.http.get<TransparencyResponseDto>(`${this.apiBase}/politicians/${accountId}/transparency`).pipe(
      map(
        (r): TransparencyReport => ({
          metrics: r.metrics.map((m) => ({
            id: m.label,
            icon: m.icon ?? 'info',
            label: m.label,
            value: formatCents(m.valueCents),
            caption: m.caption ?? '',
            period: m.period,
          })),
          expenses: r.expenseLines.map((e) => ({ category: e.category, amount: Math.round(e.amountCents / 100), share: Math.round(e.sharePercent) })),
          totalExpense: formatCents(r.totalExpenseCents),
          lastUpdate: r.lastUpdate ? `Updated ${r.lastUpdate}` : '',
        }),
      ),
      tap((transparency) => this._transparency.set(transparency)),
    );
  }

  loadCareer(accountId: string): Observable<CareerMilestone[]> {
    return this.http.get<CareerMilestoneResponseDto[]>(`${this.apiBase}/politicians/${accountId}/career`).pipe(
      map((list) => list.map((m) => ({ year: m.year, title: m.title, detail: m.detail ?? '' }))),
      tap((career) => this._career.set(career)),
    );
  }

}
