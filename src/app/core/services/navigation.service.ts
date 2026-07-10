import { computed, inject, Injectable } from '@angular/core';
import { NavItem } from '../models';
import { SessionService } from './session.service';

/** Single source of truth for the primary navigation, shared by every shell surface. */
@Injectable({ providedIn: 'root' })
export class NavigationService {
  private readonly session = inject(SessionService);

  /** Full catalogue, before any permission filtering. */
  private readonly allPrimary: NavItem[] = [
    { label: 'Feed', key: 'nav.feed', icon: 'home', route: '/feed', mobile: true },
    { label: 'Politicians', key: 'nav.politicians', icon: 'diversity_3', route: '/politicians', mobile: true },
    { label: 'Parties', key: 'nav.parties', icon: 'flag', route: '/parties', mobile: true },
    { label: 'Elections', key: 'nav.elections', icon: 'ballot', route: '/elections', mobile: true },
    { label: 'Ask AI', key: 'nav.assistant', icon: 'auto_awesome', route: '/assistant' },
    { label: 'Participation', key: 'nav.participation', icon: 'how_to_vote', route: '/participation', mobile: true, permission: 'participate' },
    { label: 'Fundraising', key: 'nav.fundraising', icon: 'volunteer_activism', route: '/fundraising', permission: 'account' },
    { label: 'Wallet', key: 'nav.wallet', icon: 'wallet', route: '/wallet', permission: 'membership' },
    { label: 'Analytics', key: 'nav.analytics', icon: 'analytics', route: '/analytics', permission: 'analytics' },
    { label: 'Party Admin', key: 'nav.party-admin', icon: 'shield_person', route: '/admin', permission: 'party-admin' },
    { label: 'Platform Admin', key: 'nav.platform-admin', icon: 'admin_panel_settings', route: '/platform', permission: 'platform-admin' },
  ];

  private readonly allSecondary: NavItem[] = [
    { label: 'Privacy & Data', key: 'nav.privacy', icon: 'shield', route: '/privacy', permission: 'account' },
  ];

  /** Items the current account is allowed to see. */
  readonly primaryItems = computed<NavItem[]>(() => this.visible(this.allPrimary));
  readonly secondaryItems = computed<NavItem[]>(() => this.visible(this.allSecondary));

  private visible(items: NavItem[]): NavItem[] {
    return items.filter((item) => !item.permission || this.session.can(item.permission));
  }
}
