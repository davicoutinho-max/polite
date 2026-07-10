import { Injectable, signal } from '@angular/core';
import {
  CareerMilestone,
  ParliamentaryActivity,
  Politician,
  ProfileTab,
  TransparencyReport,
} from '../models';

const AVATAR =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuCE0D7JPXs4LxauFS-kbprWYD0-f7RD4Ydp-sfmuPS7GeKrwOzmWLMcM8So2XYtuMo0XRoKB7SSJjtsNMISN-k8Ir3lE5sh4D9A0hBEaXTEfegcl9xBAvm-Y1HJ9KR2mu2-pRJFtTe_dLVXrLZL89YvJipXpEpEMc0Yaz6ZnDIWEpRJ8_Z4xKTl6HEZocsNuZlqHHzZi2Lnvz37jInV5Ae79N_XeulYJMqQw8VN7FXRSeKD4Uvd5UoSaA';

@Injectable({ providedIn: 'root' })
export class PoliticianService {
  readonly politician = signal<Politician>({
    id: 'jane-doe',
    name: 'Jane Doe',
    handle: '@janedoe',
    verified: true,
    party: 'Progressive Party',
    partyId: 'progressive',
    position: 'Federal Deputy, 4th District',
    servingSince: 2018,
    role: 'Federal Deputy, 4th District',
    avatarUrl: AVATAR,
    coverUrl:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuBfLxYlq8drGMj6_MPWgFtzX7vBUdily37sBqP2qqDgu6Pr4snpjPwswLUuRi551U0HSSeo-ATVCth_kekH52TSm63uIHxtXhYT7DKWwah7JKJdtNm87kTmzW-PkNjenweQVV7ArrkSGdD65jKJziFdFB8A0egariUvsXLjqr56Bv0nJnp2fxm29q89UChlOGvGWHP3_RXCyqhOKrgwXMNtBgKLzVVAMZBz0JEIdys087x9l4pwXTYjxQ',
    education: 'Master in Public Administration — State University',
    profession: 'Lawyer',
    patrimony: 'R$ 1.240.000,00',
    email: 'jane.doe@parliament.gov',
    phone: '+55 (61) 3215-0400',
    office: 'Office 402, Main Legislative Building',
    mandates: [
      { role: 'City Councilor', period: '2018 – 2020' },
      { role: 'Federal Deputy — 1st term', period: '2022 – 2026', current: true },
    ],
    socialLinks: [
      { icon: 'public', label: 'Website', handle: 'janedoe.gov', url: '#' },
      { icon: 'photo_camera', label: 'Instagram', handle: '@janedoe', url: '#' },
      { icon: 'flutter_dash', label: 'X / Twitter', handle: '@janedoe', url: '#' },
    ],
    team: [
      { name: 'Carlos Nunes', role: 'Chief of Staff', avatarUrl: AVATAR },
      { name: 'Marina Alves', role: 'Legislative Advisor', avatarUrl: AVATAR },
      { name: 'Paulo Reis', role: 'Communications', avatarUrl: AVATAR },
    ],
  }).asReadonly();

  readonly tabs = signal<ProfileTab[]>([
    { id: 'activity', label: 'Activity', key: 'tab.activity', icon: 'forum' },
    { id: 'overview', label: 'Overview', key: 'tab.overview', icon: 'badge' },
    { id: 'parliamentary', label: 'Parliamentary Activity', key: 'tab.parliamentary', icon: 'gavel' },
    { id: 'transparency', label: 'Transparency', key: 'tab.transparency', icon: 'analytics' },
    { id: 'timeline', label: 'Career Timeline', key: 'tab.career-timeline', icon: 'timeline' },
  ]).asReadonly();

  readonly activity = signal<ParliamentaryActivity>({
    projects: [
      {
        id: 'proj-1',
        reference: 'PL 452/2024',
        title: 'Clean Water Infrastructure Act',
        summary: 'Modernizes municipal water treatment and phases out lead piping over a decade.',
        status: { label: 'In Committee', severity: 'warning' },
        date: 'Mar 2024',
      },
      {
        id: 'proj-2',
        reference: 'PL 118/2023',
        title: 'Open Municipal Budget Ledger',
        summary: 'Requires a public digital ledger for every public expense above R$ 10,000.',
        status: { label: 'Active', severity: 'secondary' },
        date: 'Nov 2023',
      },
    ],
    approvedLaws: [
      {
        id: 'law-1',
        reference: 'Lei 14.220/2023',
        title: 'Public Transit Expansion',
        summary: 'Funds three new transit lines and accessible stations across every district.',
        status: { label: 'Approved', severity: 'success' },
        date: 'Aug 2023',
      },
    ],
    rejectedLaws: [
      {
        id: 'rej-1',
        reference: 'PL 87/2022',
        title: 'Urban Housing Development Act',
        summary: 'Housing incentive program that did not clear the final vote.',
        status: { label: 'Rejected', severity: 'danger' },
        date: 'Jun 2022',
      },
    ],
    pecs: [
      {
        id: 'pec-1',
        reference: 'PEC 33/2024',
        title: 'Fiscal Transparency Amendment',
        summary: 'Constitutional guarantee of open access to public spending data.',
        status: { label: 'Under Review', severity: 'warning' },
        date: 'Feb 2024',
      },
    ],
    cpis: [
      {
        id: 'cpi-1',
        reference: 'CPI 05/2023',
        title: 'Public Contracts Inquiry',
        summary: 'Investigation into procurement of municipal infrastructure contracts.',
        status: { label: 'Ongoing', severity: 'info' },
        date: '2023',
      },
    ],
    committees: [
      { name: 'Finance & Taxation', role: 'President', kind: 'committee' },
      { name: 'Constitution & Justice', role: 'Member', kind: 'committee' },
      { name: 'Digital Transparency Front', role: 'Coordinator', kind: 'front' },
      { name: 'Public Contracts Inquiry', role: 'Member', kind: 'cpi' },
    ],
    votes: [
      { id: 'v1', matter: 'PEC 33/2024 — Fiscal Transparency', date: 'Today', vote: 'yes' },
      { id: 'v2', matter: 'PL 452/2024 — Clean Water', date: '2 days ago', vote: 'yes' },
      { id: 'v3', matter: 'PL 90/2024 — Tax Reform', date: 'Last week', vote: 'no' },
      { id: 'v4', matter: 'MP 1.180 — Budget Adjustment', date: 'Last week', vote: 'abstain' },
    ],
    attendance: { present: 182, absent: 12, presenceRate: 94 },
    speeches: 47,
    interviews: 23,
    trips: 9,
  }).asReadonly();

  readonly transparency = signal<TransparencyReport>({
    metrics: [
      { id: 'm1', icon: 'payments', label: 'Salary', value: 'R$ 41.650', caption: 'Gross monthly', period: 'Jul 2026' },
      { id: 'm2', icon: 'account_balance_wallet', label: 'Parliamentary Fund', value: 'R$ 45.612', caption: 'Monthly cap', period: 'Jul 2026' },
      { id: 'm3', icon: 'redeem', label: 'Amendments', value: 'R$ 15.8M', caption: 'Allocated this term', period: '2022–2026' },
      { id: 'm4', icon: 'flight', label: 'Flights', value: 'R$ 12.340', caption: 'Official trips', period: 'Q2 2026' },
      { id: 'm5', icon: 'hotel', label: 'Per diems', value: 'R$ 8.900', caption: 'Reimbursed', period: 'Q2 2026' },
      { id: 'm6', icon: 'groups', label: 'Staff', value: '14', caption: 'Advisors on payroll', period: 'Current' },
    ],
    expenses: [
      { category: 'Office & structure', amount: 18400, share: 100 },
      { category: 'Travel & lodging', amount: 12340, share: 67 },
      { category: 'Communications', amount: 9800, share: 53 },
      { category: 'Fuel & transport', amount: 6200, share: 34 },
      { category: 'Consulting', amount: 4100, share: 22 },
    ],
    totalExpense: 'R$ 50.840',
    lastUpdate: 'Updated Jul 5, 2026',
  }).asReadonly();

  readonly career = signal<CareerMilestone[]>([
    { year: 2018, title: 'Elected City Councilor', detail: 'First public mandate, 4th District.' },
    { year: 2020, title: 'Changed party', detail: 'Joined the Progressive Party.' },
    { year: 2022, title: 'Elected Federal Deputy', detail: '112,000 votes.' },
    { year: 2023, title: 'Authored 18 bills', detail: 'Focus on transparency and infrastructure.' },
    { year: 2025, title: 'Committee President', detail: 'Finance & Taxation Committee.' },
  ]).asReadonly();
}
