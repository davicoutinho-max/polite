import { Routes } from '@angular/router';
import { Shell } from './layout/shell/shell';
import { requirePermission } from './core/guards/permission.guard';

export const routes: Routes = [
  {
    path: 'login',
    title: 'Sign In — CivicPulse',
    loadComponent: () => import('./features/auth/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'register',
    title: 'Create Account — CivicPulse',
    loadComponent: () => import('./features/auth/register-page').then((m) => m.RegisterPage),
  },
  {
    path: '',
    component: Shell,
    children: [
      { path: '', redirectTo: 'feed', pathMatch: 'full' },
      {
        path: 'feed',
        title: 'Feed — CivicPulse',
        loadComponent: () => import('./features/feed/feed').then((m) => m.Feed),
      },
      {
        path: 'alerts',
        title: 'Alerts — CivicPulse',
        canMatch: [requirePermission('account')],
        loadComponent: () => import('./features/alerts/alerts-page').then((m) => m.AlertsPage),
      },
      {
        path: 'participation',
        title: 'Citizen Participation — CivicPulse',
        canMatch: [requirePermission(['participate', 'create-participation'])],
        loadComponent: () =>
          import('./features/participation/participation-page').then((m) => m.ParticipationPage),
      },
      {
        path: 'assistant',
        title: 'Ask AI — CivicPulse',
        loadComponent: () => import('./features/assistant/assistant-page').then((m) => m.AssistantPage),
      },
      {
        path: 'fundraising',
        title: 'Fundraising — CivicPulse',
        canMatch: [requirePermission('account')],
        loadComponent: () =>
          import('./features/fundraising/fundraising-page').then((m) => m.FundraisingPage),
      },
      {
        path: 'messages',
        title: 'Messages — CivicPulse',
        canMatch: [requirePermission('message')],
        loadComponent: () => import('./features/messages/messages-page').then((m) => m.MessagesPage),
      },
      {
        path: 'wallet',
        title: 'Digital Wallet — CivicPulse',
        canMatch: [requirePermission('membership')],
        loadComponent: () => import('./features/wallet/wallet-page').then((m) => m.WalletPage),
      },
      {
        path: 'admin',
        title: 'Party Admin — CivicPulse',
        canMatch: [requirePermission('party-admin')],
        loadComponent: () => import('./features/admin/admin-page').then((m) => m.AdminPage),
      },
      {
        path: 'platform',
        title: 'Platform Admin — CivicPulse',
        canMatch: [requirePermission('platform-admin')],
        loadComponent: () => import('./features/platform/platform-page').then((m) => m.PlatformPage),
      },
      {
        path: 'analytics',
        title: 'Analytics — CivicPulse',
        canMatch: [requirePermission('analytics')],
        loadComponent: () => import('./features/analytics/analytics-page').then((m) => m.AnalyticsPage),
      },
      {
        path: 'privacy',
        title: 'Privacy & Data — CivicPulse',
        canMatch: [requirePermission('account')],
        loadComponent: () => import('./features/privacy/privacy-page').then((m) => m.PrivacyPage),
      },
      {
        path: 'politicians',
        title: 'Politicians — CivicPulse',
        loadComponent: () =>
          import('./features/politicians/politicians-page').then((m) => m.PoliticiansPage),
      },
      {
        path: 'parties',
        title: 'Parties — CivicPulse',
        loadComponent: () => import('./features/parties/parties-page').then((m) => m.PartiesPage),
      },
      {
        path: 'elections',
        title: 'Elections — CivicPulse',
        loadComponent: () => import('./features/elections/elections-page').then((m) => m.ElectionsPage),
      },
      {
        path: 'elections/:id',
        title: 'Election — CivicPulse',
        loadComponent: () =>
          import('./features/elections/election-detail/election-detail').then((m) => m.ElectionDetailPage),
      },
      {
        path: 'party/:id',
        title: 'Party — CivicPulse',
        loadComponent: () => import('./features/party/party-page').then((m) => m.PartyPage),
      },
      {
        path: 'profile/:id',
        title: 'Profile — CivicPulse',
        loadComponent: () => import('./features/profile/profile').then((m) => m.Profile),
      },
      {
        path: 'profile',
        title: 'Profile — CivicPulse',
        canMatch: [requirePermission('account')],
        loadComponent: () => import('./features/profile/profile').then((m) => m.Profile),
      },
    ],
  },
  { path: '**', redirectTo: 'feed' },
];
