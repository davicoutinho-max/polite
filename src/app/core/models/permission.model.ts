/**
 * Account types on the platform:
 * - visitor:    not signed in — public content only.
 * - citizen:    a regular person (pessoa física) — follows, participates, may
 *               request affiliation to a party. May support/contribute to
 *               causes but not create them.
 * - politician: a public figure registered by a party (CPF/CNPJ verification
 *               collected at registration time), who publishes content and
 *               may create fundraising causes, petitions/consultations/surveys,
 *               and group chats with other politicians/parties.
 * - party:      manages a party's presence (members, content, events,
 *               analytics), registers politicians under it, and — like
 *               politicians — may create fundraising causes,
 *               petitions/consultations/surveys, and group chats.
 * - admin:      platform administrator — creates/edits parties and handles
 *               independent-politician reassignment from a settings screen.
 */
export type AccountType = 'visitor' | 'citizen' | 'politician' | 'party' | 'admin';

export type Permission =
  | 'view-public' // read public posts from parties and politicians
  | 'account' // any signed-in account
  | 'follow' // follow politicians and parties (personal accounts)
  | 'react' // like / comment / share
  | 'message' // direct messages
  | 'participate' // vote on bills/PECs, sign petitions, answer surveys (opinion)
  | 'request-affiliation' // request party affiliation
  | 'membership' // digital membership card / affiliation area
  | 'create-fundraiser' // create fundraising actions (politicians & parties)
  | 'create-participation' // create petitions/consultations/surveys (politicians & parties)
  | 'publish-content' // publish posts / news (politicians & parties)
  | 'party-admin' // manage a party's presence
  | 'platform-admin' // platform administration (create parties, assign politicians)
  | 'analytics'; // engagement dashboards

export const TYPE_PERMISSIONS: Record<AccountType, readonly Permission[]> = {
  visitor: ['view-public'],
  citizen: [
    'view-public',
    'account',
    'follow',
    'react',
    'message',
    'participate',
    'request-affiliation',
    'membership',
  ],
  politician: [
    'view-public',
    'account',
    'follow',
    'react',
    'message',
    'publish-content',
    'create-fundraiser',
    'create-participation',
    'analytics',
  ],
  party: [
    'view-public',
    'account',
    'react',
    'message',
    'publish-content',
    'create-fundraiser',
    'create-participation',
    'party-admin',
    'analytics',
  ],
  admin: ['view-public', 'account', 'message', 'platform-admin', 'party-admin', 'analytics'],
};

export interface AccountTypeOption {
  readonly type: AccountType;
  readonly label: string;
  readonly description: string;
  readonly icon: string;
}

/** Catalogue used by the demo account-type switcher. */
export const ACCOUNT_TYPE_OPTIONS: readonly AccountTypeOption[] = [
  {
    type: 'visitor',
    label: 'Visitor',
    description: 'Not signed in — reads public content only.',
    icon: 'visibility',
  },
  {
    type: 'citizen',
    label: 'Citizen',
    description: 'Regular person — follows, participates and may affiliate.',
    icon: 'person',
  },
  {
    type: 'politician',
    label: 'Politician',
    description: 'Public figure — manages their profile and publishes.',
    icon: 'campaign',
  },
  {
    type: 'party',
    label: 'Party',
    description: 'Manages members, content, events and analytics.',
    icon: 'flag',
  },
  {
    type: 'admin',
    label: 'Platform Admin',
    description: 'Creates parties and assigns politicians to them.',
    icon: 'admin_panel_settings',
  },
];
