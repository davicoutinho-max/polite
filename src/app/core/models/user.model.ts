/** A person shown across the app (author, politician, current user). */
export interface UserSummary {
  readonly id: string;
  readonly name: string;
  readonly handle?: string;
  readonly avatarUrl: string;
  readonly verified?: boolean;
  /** Contextual subtitle, e.g. "State Legislature" or "Progressive Party". */
  readonly role?: string;
}

/**
 * The signed-in account. Extends the display summary with the account type,
 * which drives every permission. See {@link AccountType}.
 */
export interface Account extends UserSummary {
  readonly accountType: import('./permission.model').AccountType;
}
