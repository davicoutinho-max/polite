import { computed, Injectable, signal } from '@angular/core';
import { Account, AccountType, Permission, TYPE_PERMISSIONS, UserSummary } from '../models';

const CITIZEN_AVATAR =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuARrPtz8cdX-XEjmt6mzDr-yEB20GT86Vn2pwKXi-5JWa3WGOtpu2UZ53Clzs2UcsoUoRwjb6wjw4AUdOdgkX173o7MCsccQ_OhfJNR75fdsj3a5mJYH-bXhcLbpBI1-z4fVeifFnFeEQQKMdwNjq0xdG4H2KkmEDaK3ibUiLFVAb-mCXTgCg2zRPjR05v5YuxVH-JTO2o9dQN3hagJW1O1M_Tkor9T5VNVg8T-Ui3Hh1LYLnroVzxx0g';

const GUEST_AVATAR =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 40 40'%3E%3Crect width='40' height='40' fill='%23c7ccd1'/%3E%3Ccircle cx='20' cy='15' r='7' fill='%23fff'/%3E%3Cpath d='M6 38c0-8 6-13 14-13s14 5 14 13z' fill='%23fff'/%3E%3C/svg%3E";

/** One sample identity per account type, so the demo switcher feels realistic. */
const SAMPLE_ACCOUNTS: Record<AccountType, Account> = {
  visitor: {
    id: 'guest',
    name: 'Visitor',
    handle: '@guest',
    role: 'Not signed in',
    accountType: 'visitor',
    avatarUrl: GUEST_AVATAR,
  },
  citizen: {
    id: 'me',
    name: 'Alex Morgan',
    handle: '@alex.morgan',
    role: 'Citizen',
    accountType: 'citizen',
    avatarUrl: CITIZEN_AVATAR,
  },
  politician: {
    id: 'jane-doe',
    name: 'Jane Doe',
    handle: '@jane.doe',
    role: 'Federal Deputy · Progressive Party',
    verified: true,
    accountType: 'politician',
    avatarUrl: CITIZEN_AVATAR,
  },
  party: {
    id: 'progressive',
    name: 'Progressive Party',
    handle: '@progressive',
    role: 'Official party account',
    verified: true,
    accountType: 'party',
    avatarUrl: CITIZEN_AVATAR,
  },
  admin: {
    id: 'platform-admin',
    name: 'Platform Admin',
    handle: '@admin',
    role: 'Platform administrator',
    accountType: 'admin',
    avatarUrl: GUEST_AVATAR,
  },
};

/**
 * Holds the current account and derives its permission set from the account
 * type. The type can be switched at runtime (demo control) so the whole UI
 * reacts to what each kind of account — visitor, citizen, politician, party,
 * admin — is allowed to see and do.
 */
@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly _account = signal<Account>(SAMPLE_ACCOUNTS.citizen);

  readonly account = this._account.asReadonly();
  /** Kept for backward compatibility with components reading a user summary. */
  readonly currentUser = computed<UserSummary>(() => this._account());

  readonly type = computed<AccountType>(() => this._account().accountType);
  readonly isVisitor = computed(() => this.type() === 'visitor');
  readonly isAuthenticated = computed(() => this.type() !== 'visitor');

  readonly permissions = computed<ReadonlySet<Permission>>(
    () => new Set<Permission>(TYPE_PERMISSIONS[this.type()]),
  );

  can(permission: Permission): boolean {
    return this.permissions().has(permission);
  }

  /** Demo helper: switch the acting account type. */
  setType(type: AccountType): void {
    this._account.set(SAMPLE_ACCOUNTS[type]);
  }

  logout(): void {
    this._account.set(SAMPLE_ACCOUNTS.visitor);
  }
}
