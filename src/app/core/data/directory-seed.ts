import { GovLevel, PartySummary, PoliticianSummary } from '../models';

const AVATARS = [
  'https://lh3.googleusercontent.com/aida-public/AB6AXuARrPtz8cdX-XEjmt6mzDr-yEB20GT86Vn2pwKXi-5JWa3WGOtpu2UZ53Clzs2UcsoUoRwjb6wjw4AUdOdgkX173o7MCsccQ_OhfJNR75fdsj3a5mJYH-bXhcLbpBI1-z4fVeifFnFeEQQKMdwNjq0xdG4H2KkmEDaK3ibUiLFVAb-mCXTgCg2zRPjR05v5YuxVH-JTO2o9dQN3hagJW1O1M_Tkor9T5VNVg8T-Ui3Hh1LYLnroVzxx0g',
  'https://lh3.googleusercontent.com/aida-public/AB6AXuDNF06tFrfgVfuEw_FOHQKoJQ-FGxIeD-WKKUf4bFAfJRNfHinpNj7IPKjXwJZ-BwhQhF5TBOJdOcahM6PA4rSbKCvV0Y9GUSm748-U1fFOS7Tv6AEJ-U6nJK75Cp_U9uPx1ebSN9gYtaN1t7AC4T7l2iXovmj25qvTxScJgZ0D0MVacpDHIs87kOvXrgibiMZj9zmtR_Oyed2kt01LUJlA5h_EHb7Yp1Ie1MVH0QLC5Bs06fXy4OuKpw',
];

export const PARTY_SEED: PartySummary[] = [
  { id: 'progressive', name: 'Progressive Party', acronym: 'PP', number: 45, logoUrl: AVATARS[0], spectrum: 'center-left', ideology: 'Social democracy', members: 148230, founded: 1998 },
  { id: 'liberty', name: 'Liberty Alliance', acronym: 'LA', number: 22, logoUrl: AVATARS[1], spectrum: 'center-right', ideology: 'Classical liberalism', members: 91540, founded: 2001 },
  { id: 'green-future', name: 'Green Future', acronym: 'GF', number: 30, logoUrl: AVATARS[0], spectrum: 'left', ideology: 'Green politics', members: 47210, founded: 2010 },
  { id: 'national-union', name: 'National Union', acronym: 'NU', number: 15, logoUrl: AVATARS[1], spectrum: 'right', ideology: 'Conservatism', members: 132900, founded: 1989 },
  { id: 'workers-front', name: "Workers' Front", acronym: 'WF', number: 13, logoUrl: AVATARS[0], spectrum: 'left', ideology: 'Democratic socialism', members: 110450, founded: 1985 },
  { id: 'civic-center', name: 'Civic Center', acronym: 'CC', number: 25, logoUrl: AVATARS[1], spectrum: 'center', ideology: 'Centrism', members: 68320, founded: 2005 },
  { id: 'renewal', name: 'Renewal Movement', acronym: 'RM', number: 40, logoUrl: AVATARS[0], spectrum: 'center-right', ideology: 'Reformism', members: 54110, founded: 2014 },
  { id: 'popular-democratic', name: 'Popular Democratic', acronym: 'PD', number: 12, logoUrl: AVATARS[1], spectrum: 'center-left', ideology: 'Progressivism', members: 87600, founded: 1996 },
  { id: 'freedom', name: 'Freedom Party', acronym: 'FP', number: 18, logoUrl: AVATARS[0], spectrum: 'right', ideology: 'Libertarianism', members: 39980, founded: 2016 },
  { id: 'unity', name: 'Unity Coalition', acronym: 'UC', number: 27, logoUrl: AVATARS[1], spectrum: 'center', ideology: 'Big tent', members: 72340, founded: 2008 },
];

const FIRST = ['Ana', 'Bruno', 'Carla', 'Diego', 'Elena', 'Felipe', 'Gabriela', 'Hugo', 'Isabela', 'João', 'Karina', 'Lucas', 'Marina', 'Nuno', 'Olívia', 'Paulo', 'Rita', 'Sofia', 'Tiago', 'Vera'];
const LAST = ['Almeida', 'Barros', 'Costa', 'Dias', 'Esteves', 'Faria', 'Gomes', 'Henriques', 'Lima', 'Moreira', 'Nunes', 'Oliveira', 'Pereira', 'Queiroz', 'Ramos', 'Santos', 'Tavares', 'Vieira'];
const STATES = ['SP', 'RJ', 'MG', 'RS', 'BA', 'PR', 'PE', 'CE', 'SC', 'GO', 'DF', 'PA'];

const OFFICES: Record<GovLevel, string[]> = {
  federal: ['Federal Deputy', 'Senator'],
  state: ['State Representative', 'Governor'],
  municipal: ['City Councilor', 'Mayor'],
};
const LEVELS: GovLevel[] = ['federal', 'state', 'municipal'];

/** Deterministically generate a sizeable politician directory for the listing. */
function buildPoliticians(): PoliticianSummary[] {
  const people: PoliticianSummary[] = [];
  const count = 48;
  for (let i = 0; i < count; i++) {
    const first = FIRST[(i * 7) % FIRST.length];
    const last = LAST[(i * 5 + 3) % LAST.length];
    const level = LEVELS[i % LEVELS.length];
    const office = OFFICES[level][i % OFFICES[level].length];
    const party = PARTY_SEED[(i * 3) % PARTY_SEED.length];
    const state = STATES[(i * 4 + 1) % STATES.length];
    const followers = 1200 + ((i * 3779) % 480000);
    const billsCount = 2 + ((i * 13) % 60);
    people.push({
      id: `pol-${i + 1}`,
      name: `${first} ${last}`,
      handle: `@${first.toLowerCase()}.${last.toLowerCase()}`,
      avatarUrl: AVATARS[i % AVATARS.length],
      verified: i % 3 === 0,
      office,
      level,
      partyId: party.id,
      partyAcronym: party.acronym,
      state,
      followers,
      billsCount,
    });
  }
  return people;
}

export const POLITICIAN_SEED: PoliticianSummary[] = buildPoliticians();
