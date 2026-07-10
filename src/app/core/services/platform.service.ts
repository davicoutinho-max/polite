import { computed, Injectable, signal } from '@angular/core';
import {
  NewParty,
  PartyRegistryEntry,
  PlatformCountry,
  PlatformLanguage,
  PlatformState,
  PoliticianDirectoryEntry,
  TranslationEntry,
} from '../models';

/**
 * Platform-administration state: the party registry and the politician
 * directory. Creating/editing parties and assigning politicians to them are
 * exclusive platform-admin actions, per the described flow.
 */
@Injectable({ providedIn: 'root' })
export class PlatformService {
  private readonly _parties = signal<PartyRegistryEntry[]>([
    {
      id: 'progressive',
      name: 'Progressive Party',
      acronym: 'PP',
      number: 45,
      president: 'Sen. Laura Prado',
      ideology: 'Social democracy · Progressivism',
      memberCount: 148230,
    },
    {
      id: 'liberty',
      name: 'Liberty Alliance',
      acronym: 'LA',
      number: 22,
      president: 'Rep. Otto Braun',
      ideology: 'Classical liberalism',
      memberCount: 91540,
    },
  ]);

  private readonly _politicians = signal<PoliticianDirectoryEntry[]>([
    { id: 'jane-doe', name: 'Jane Doe', office: 'Federal Deputy', partyId: 'progressive' },
    { id: 'laura-prado', name: 'Laura Prado', office: 'Senator', partyId: 'progressive' },
    { id: 'marcus-chen', name: 'Marcus Chen', office: 'City Councilor', partyId: 'liberty' },
    { id: 'rita-sa', name: 'Rita Sá', office: 'Mayor', partyId: null },
    { id: 'diego-faria', name: 'Diego Faria', office: 'State Representative', partyId: null },
  ]);

  readonly parties = this._parties.asReadonly();
  readonly politicians = this._politicians.asReadonly();

  readonly unassigned = computed(() => this._politicians().filter((p) => p.partyId === null));

  partyName(id: string | null): string {
    if (!id) {
      return 'Independent';
    }
    return this._parties().find((p) => p.id === id)?.name ?? 'Independent';
  }

  politiciansOf(partyId: string): PoliticianDirectoryEntry[] {
    return this._politicians().filter((p) => p.partyId === partyId);
  }

  createParty(input: NewParty): void {
    const id = input.acronym.toLowerCase().replace(/[^a-z0-9]+/g, '-') || `party-${Date.now()}`;
    const entry: PartyRegistryEntry = {
      id: this._parties().some((p) => p.id === id) ? `${id}-${Date.now()}` : id,
      name: input.name,
      acronym: input.acronym,
      number: input.number,
      president: input.president,
      ideology: input.ideology,
      memberCount: 0,
    };
    this._parties.update((list) => [...list, entry]);
  }

  updatePresident(partyId: string, president: string): void {
    this._parties.update((list) =>
      list.map((p) => (p.id === partyId ? { ...p, president } : p)),
    );
  }

  assignPolitician(politicianId: string, partyId: string | null): void {
    this._politicians.update((list) =>
      list.map((p) => (p.id === politicianId ? { ...p, partyId } : p)),
    );
  }

  // ---- Platform parameters: countries & states ----
  private readonly _countries = signal<PlatformCountry[]>([
    { id: 'br', name: 'Brazil', code: 'BR' },
    { id: 'pt', name: 'Portugal', code: 'PT' },
    { id: 'us', name: 'United States', code: 'US' },
  ]);
  readonly countries = this._countries.asReadonly();

  private readonly _states = signal<PlatformState[]>([
    { id: 'sp', name: 'São Paulo', code: 'SP', countryId: 'br' },
    { id: 'rj', name: 'Rio de Janeiro', code: 'RJ', countryId: 'br' },
    { id: 'mg', name: 'Minas Gerais', code: 'MG', countryId: 'br' },
    { id: 'rs', name: 'Rio Grande do Sul', code: 'RS', countryId: 'br' },
    { id: 'df', name: 'Distrito Federal', code: 'DF', countryId: 'br' },
  ]);
  readonly states = this._states.asReadonly();

  countryName(id: string): string {
    return this._countries().find((c) => c.id === id)?.name ?? '—';
  }

  statesOf(countryId: string): PlatformState[] {
    return this._states().filter((s) => s.countryId === countryId);
  }

  addCountry(name: string, code: string): void {
    const id = this.slugify(code || name);
    if (this._countries().some((c) => c.id === id)) return;
    this._countries.update((list) => [...list, { id, name, code: code.toUpperCase() }]);
  }

  removeCountry(id: string): void {
    this._countries.update((list) => list.filter((c) => c.id !== id));
    this._states.update((list) => list.filter((s) => s.countryId !== id));
  }

  addState(name: string, code: string, countryId: string): void {
    const id = this.slugify(code || name);
    if (this._states().some((s) => s.id === id)) return;
    this._states.update((list) => [...list, { id, name, code: code.toUpperCase(), countryId }]);
  }

  removeState(id: string): void {
    this._states.update((list) => list.filter((s) => s.id !== id));
  }

  // ---- Platform parameters: languages ----
  private readonly _languages = signal<PlatformLanguage[]>([
    { id: 'pt-br', name: 'Português (Brasil)', code: 'pt-BR', isDefault: true },
    { id: 'en-us', name: 'English (US)', code: 'en-US', isDefault: false },
  ]);
  readonly languages = this._languages.asReadonly();

  addLanguage(name: string, code: string): void {
    const id = this.slugify(code || name);
    if (this._languages().some((l) => l.id === id)) return;
    this._languages.update((list) => [...list, { id, name, code, isDefault: list.length === 0 }]);
    this._translations.update((list) =>
      list.map((t) => ({ ...t, values: { ...t.values, [id]: t.values[id] ?? '' } })),
    );
  }

  removeLanguage(id: string): void {
    this._languages.update((list) => {
      const remaining = list.filter((l) => l.id !== id);
      const removed = list.find((l) => l.id === id);
      if (removed?.isDefault && remaining.length && !remaining.some((l) => l.isDefault)) {
        remaining[0] = { ...remaining[0], isDefault: true };
      }
      return remaining;
    });
    this._translations.update((list) =>
      list.map((t) => {
        const values = { ...t.values };
        delete values[id];
        return { ...t, values };
      }),
    );
  }

  setDefaultLanguage(id: string): void {
    this._languages.update((list) => list.map((l) => ({ ...l, isDefault: l.id === id })));
  }

  // ---- Platform parameters: translation tags ----
  private readonly _translations = signal<TranslationEntry[]>([
    // ---- Navigation ----
    { id: 't-nav-feed', key: 'nav.feed', values: { 'pt-br': 'Início', 'en-us': 'Feed' } },
    { id: 't-nav-politicians', key: 'nav.politicians', values: { 'pt-br': 'Políticos', 'en-us': 'Politicians' } },
    { id: 't-nav-parties', key: 'nav.parties', values: { 'pt-br': 'Partidos', 'en-us': 'Parties' } },
    { id: 't-nav-elections', key: 'nav.elections', values: { 'pt-br': 'Eleições', 'en-us': 'Elections' } },
    { id: 't-nav-assistant', key: 'nav.assistant', values: { 'pt-br': 'Perguntar à IA', 'en-us': 'Ask AI' } },
    { id: 't-nav-participation', key: 'nav.participation', values: { 'pt-br': 'Participação', 'en-us': 'Participation' } },
    { id: 't-nav-fundraising', key: 'nav.fundraising', values: { 'pt-br': 'Arrecadação', 'en-us': 'Fundraising' } },
    { id: 't-nav-wallet', key: 'nav.wallet', values: { 'pt-br': 'Carteira', 'en-us': 'Wallet' } },
    { id: 't-nav-analytics', key: 'nav.analytics', values: { 'pt-br': 'Análises', 'en-us': 'Analytics' } },
    { id: 't-nav-party-admin', key: 'nav.party-admin', values: { 'pt-br': 'Admin do Partido', 'en-us': 'Party Admin' } },
    { id: 't-nav-platform-admin', key: 'nav.platform-admin', values: { 'pt-br': 'Admin da Plataforma', 'en-us': 'Platform Admin' } },
    { id: 't-nav-privacy', key: 'nav.privacy', values: { 'pt-br': 'Privacidade e Dados', 'en-us': 'Privacy & Data' } },

    // ---- Shared chrome ----
    { id: 't-chrome-light-mode', key: 'chrome.light-mode', values: { 'pt-br': 'Modo claro', 'en-us': 'Light mode' } },
    { id: 't-chrome-dark-mode', key: 'chrome.dark-mode', values: { 'pt-br': 'Modo escuro', 'en-us': 'Dark mode' } },
    { id: 't-chrome-create-post', key: 'chrome.create-post', values: { 'pt-br': 'Criar publicação', 'en-us': 'Create post' } },
    { id: 't-chrome-join-discussion', key: 'chrome.join-discussion', values: { 'pt-br': 'Participar da discussão', 'en-us': 'Join discussion' } },
    { id: 't-chrome-brand-subtitle', key: 'chrome.brand-subtitle', values: { 'pt-br': 'Transparência Institucional', 'en-us': 'Institutional Transparency' } },

    // ---- Account menu ----
    { id: 't-account-testing-as', key: 'account.testing-as', values: { 'pt-br': 'Testando como (MVP)', 'en-us': 'Testing as (MVP)' } },
    { id: 't-account-my-profile', key: 'account.my-profile', values: { 'pt-br': 'Meu perfil', 'en-us': 'My profile' } },
    { id: 't-account-privacy', key: 'account.privacy', values: { 'pt-br': 'Privacidade e dados', 'en-us': 'Privacy & data' } },
    { id: 't-account-sign-out', key: 'account.sign-out', values: { 'pt-br': 'Sair', 'en-us': 'Sign out' } },
    { id: 't-account-log-in', key: 'account.log-in', values: { 'pt-br': 'Entrar', 'en-us': 'Log in' } },
    { id: 't-account-create-account', key: 'account.create-account', values: { 'pt-br': 'Criar conta', 'en-us': 'Create account' } },
    { id: 't-account-type-visitor', key: 'account.type.visitor', values: { 'pt-br': 'Visitante', 'en-us': 'Visitor' } },
    { id: 't-account-type-citizen', key: 'account.type.citizen', values: { 'pt-br': 'Cidadão', 'en-us': 'Citizen' } },
    { id: 't-account-type-politician', key: 'account.type.politician', values: { 'pt-br': 'Político', 'en-us': 'Politician' } },
    { id: 't-account-type-party', key: 'account.type.party', values: { 'pt-br': 'Partido', 'en-us': 'Party' } },
    { id: 't-account-type-admin', key: 'account.type.admin', values: { 'pt-br': 'Admin da Plataforma', 'en-us': 'Platform Admin' } },

    // ---- Page headers ----
    { id: 't-page-wallet-title', key: 'page.wallet.title', values: { 'pt-br': 'Carteira Digital', 'en-us': 'Digital Wallet' } },
    { id: 't-page-wallet-subtitle', key: 'page.wallet.subtitle', values: { 'pt-br': 'Seu cartão de associado, filiação e contribuição mensal', 'en-us': 'Your membership card, affiliation and monthly contribution' } },
    { id: 't-page-alerts-title', key: 'page.alerts.title', values: { 'pt-br': 'Alertas', 'en-us': 'Alerts' } },
    { id: 't-page-alerts-unread', key: 'page.alerts.unread', values: { 'pt-br': 'notificações não lidas', 'en-us': 'unread notifications' } },
    { id: 't-page-fundraising-title', key: 'page.fundraising.title', values: { 'pt-br': 'Arrecadação', 'en-us': 'Fundraising' } },
    { id: 't-page-fundraising-subtitle', key: 'page.fundraising.subtitle', values: { 'pt-br': 'Causas sociais e iniciativas partidárias', 'en-us': 'Social causes and party initiatives' } },
    { id: 't-page-privacy-title', key: 'page.privacy.title', values: { 'pt-br': 'Privacidade e Dados', 'en-us': 'Privacy & Data' } },
    { id: 't-page-privacy-subtitle', key: 'page.privacy.subtitle', values: { 'pt-br': 'Controle seu consentimento e exerça seus direitos sobre dados', 'en-us': 'Control your consent and exercise your data rights' } },
    { id: 't-page-admin-suffix', key: 'page.admin.suffix', values: { 'pt-br': 'Admin', 'en-us': 'Admin' } },
    { id: 't-page-admin-subtitle', key: 'page.admin.subtitle', values: { 'pt-br': 'Gestão de membros, aprovações e engajamento', 'en-us': 'Member management, approvals and engagement' } },
    { id: 't-page-assistant-title', key: 'page.assistant.title', values: { 'pt-br': 'Perguntar à IA', 'en-us': 'Ask AI' } },
    { id: 't-page-assistant-subtitle', key: 'page.assistant.subtitle', values: { 'pt-br': 'Entenda projetos de lei, PECs e inquéritos em linguagem simples', 'en-us': 'Understand bills, PECs and inquiries in plain language' } },
    { id: 't-page-analytics-title', key: 'page.analytics.title', values: { 'pt-br': 'Análises', 'en-us': 'Analytics' } },
    { id: 't-page-analytics-subtitle', key: 'page.analytics.subtitle', values: { 'pt-br': 'Alcance, engajamento e informações sobre o público', 'en-us': 'Reach, engagement and audience insights' } },
    { id: 't-page-politicians-title', key: 'page.politicians.title', values: { 'pt-br': 'Políticos', 'en-us': 'Politicians' } },
    { id: 't-page-politicians-subtitle', key: 'page.politicians.subtitle', values: { 'pt-br': 'Explore todos os políticos da plataforma', 'en-us': 'Explore every politician on the platform' } },
    { id: 't-page-timeline-title', key: 'page.timeline.title', values: { 'pt-br': 'Linha do Tempo', 'en-us': 'Timeline' } },
    { id: 't-page-timeline-subtitle', key: 'page.timeline.subtitle', values: { 'pt-br': 'Atividades relevantes de quem você segue', 'en-us': 'Smart activity from the people and parties you follow' } },
    { id: 't-page-parties-title', key: 'page.parties.title', values: { 'pt-br': 'Partidos', 'en-us': 'Parties' } },
    { id: 't-page-parties-subtitle', key: 'page.parties.subtitle', values: { 'pt-br': 'Navegue e filtre todos os partidos da plataforma', 'en-us': 'Browse and filter every party on the platform' } },
    { id: 't-page-participation-title', key: 'page.participation.title', values: { 'pt-br': 'Participação Cidadã', 'en-us': 'Citizen Participation' } },
    { id: 't-page-participation-subtitle', key: 'page.participation.subtitle', values: { 'pt-br': 'Assine petições, opine em consultas e responda pesquisas', 'en-us': 'Sign petitions, weigh in on consultations and answer surveys' } },
    { id: 't-page-platform-title', key: 'page.platform.title', values: { 'pt-br': 'Administração da Plataforma', 'en-us': 'Platform Administration' } },
    { id: 't-page-platform-subtitle', key: 'page.platform.subtitle', values: { 'pt-br': 'Registro de partidos, atribuição de políticos e parâmetros da plataforma', 'en-us': 'Party registry, politician assignment and platform-wide parameters' } },
    { id: 't-page-elections-title', key: 'page.elections.title', values: { 'pt-br': 'Eleições', 'en-us': 'Elections' } },
    { id: 't-page-elections-subtitle', key: 'page.elections.subtitle', values: { 'pt-br': 'Calendário eleitoral e candidatos', 'en-us': 'Election calendar and candidates' } },

    // ---- Common ----
    { id: 't-common-save', key: 'common.save', values: { 'pt-br': 'Salvar', 'en-us': 'Save' } },
    { id: 't-common-cancel', key: 'common.cancel', values: { 'pt-br': 'Cancelar', 'en-us': 'Cancel' } },

    // ---- Bulk i18n coverage pass (auto-generated) ----
    { id: 't-a11y-new-conversation', key: 'a11y.new-conversation', values: { 'pt-br': 'Iniciar uma nova conversa', 'en-us': 'Start a new conversation' } },
    { id: 't-a11y-unread', key: 'a11y.unread', values: { 'pt-br': 'Não lida', 'en-us': 'Unread' } },
    { id: 't-action-create-event', key: 'action.create-event', values: { 'pt-br': 'Criar evento', 'en-us': 'Create event' } },
    { id: 't-action-create-event-desc', key: 'action.create-event.desc', values: { 'pt-br': 'Convenções, cursos, fóruns', 'en-us': 'Conventions, courses, forums' } },
    { id: 't-action-create-survey', key: 'action.create-survey', values: { 'pt-br': 'Criar pesquisa', 'en-us': 'Create survey' } },
    { id: 't-action-create-survey-desc', key: 'action.create-survey.desc', values: { 'pt-br': 'Colete a opinião dos membros', 'en-us': 'Collect member opinions' } },
    { id: 't-action-publish-content', key: 'action.publish-content', values: { 'pt-br': 'Publicar conteúdo', 'en-us': 'Publish content' } },
    { id: 't-action-publish-content-desc', key: 'action.publish-content.desc', values: { 'pt-br': 'Compartilhe novidades com sua base', 'en-us': 'Share news with your base' } },
    { id: 't-action-send-notification', key: 'action.send-notification', values: { 'pt-br': 'Enviar notificação', 'en-us': 'Send notification' } },
    { id: 't-action-send-notification-desc', key: 'action.send-notification.desc', values: { 'pt-br': 'Alcance todos os membros', 'en-us': 'Reach all members' } },
    { id: 't-button-add', key: 'button.add', values: { 'pt-br': 'Adicionar', 'en-us': 'Add' } },
    { id: 't-button-add-key', key: 'button.add-key', values: { 'pt-br': 'Adicionar chave', 'en-us': 'Add key' } },
    { id: 't-button-approve', key: 'button.approve', values: { 'pt-br': 'Aprovar', 'en-us': 'Approve' } },
    { id: 't-button-cancel', key: 'button.cancel', values: { 'pt-br': 'Cancelar', 'en-us': 'Cancel' } },
    { id: 't-button-clear-conversation', key: 'button.clear-conversation', values: { 'pt-br': 'Limpar conversa', 'en-us': 'Clear conversation' } },
    { id: 't-button-clear-filters', key: 'button.clear-filters', values: { 'pt-br': 'Limpar filtros', 'en-us': 'Clear filters' } },
    { id: 't-button-close', key: 'button.close', values: { 'pt-br': 'Fechar', 'en-us': 'Close' } },
    { id: 't-button-confirm-deletion', key: 'button.confirm-deletion', values: { 'pt-br': 'Confirmar exclusão', 'en-us': 'Confirm deletion' } },
    { id: 't-button-contact', key: 'button.contact', values: { 'pt-br': 'Contato', 'en-us': 'Contact' } },
    { id: 't-button-continue-as-visitor', key: 'button.continue-as-visitor', values: { 'pt-br': 'Continuar como visitante', 'en-us': 'Continue as a visitor' } },
    { id: 't-button-contribute', key: 'button.contribute', values: { 'pt-br': 'Contribuir', 'en-us': 'Contribute' } },
    { id: 't-button-create-account', key: 'button.create-account', values: { 'pt-br': 'Criar conta', 'en-us': 'Create account' } },
    { id: 't-button-create-one', key: 'button.create-one', values: { 'pt-br': 'Criar uma', 'en-us': 'Create one' } },
    { id: 't-button-create-party', key: 'button.create-party', values: { 'pt-br': 'Criar partido', 'en-us': 'Create party' } },
    { id: 't-button-delete', key: 'button.delete', values: { 'pt-br': 'Excluir', 'en-us': 'Delete' } },
    { id: 't-button-export', key: 'button.export', values: { 'pt-br': 'Exportar', 'en-us': 'Export' } },
    { id: 't-button-follow', key: 'button.follow', values: { 'pt-br': 'Seguir', 'en-us': 'Follow' } },
    { id: 't-button-following', key: 'button.following', values: { 'pt-br': 'Seguindo', 'en-us': 'Following' } },
    { id: 't-button-join-party', key: 'button.join-party', values: { 'pt-br': 'Filiar-se', 'en-us': 'Join party' } },
    { id: 't-button-link', key: 'button.link', values: { 'pt-br': 'Vincular', 'en-us': 'Link' } },
    { id: 't-button-mark-all-read', key: 'button.mark-all-read', values: { 'pt-br': 'Marcar tudo como lido', 'en-us': 'Mark all as read' } },
    { id: 't-button-pay-contribution', key: 'button.pay-contribution', values: { 'pt-br': 'Pagar contribuição', 'en-us': 'Pay contribution' } },
    { id: 't-button-publish-action', key: 'button.publish-action', values: { 'pt-br': 'Publicar ação', 'en-us': 'Publish action' } },
    { id: 't-button-read-statute', key: 'button.read-statute', values: { 'pt-br': 'Ler o estatuto', 'en-us': 'Read the statute' } },
    { id: 't-button-register-link', key: 'button.register-link', values: { 'pt-br': 'Cadastrar e vincular', 'en-us': 'Register & link' } },
    { id: 't-button-reject', key: 'button.reject', values: { 'pt-br': 'Rejeitar', 'en-us': 'Reject' } },
    { id: 't-button-remove', key: 'button.remove', values: { 'pt-br': 'Remover', 'en-us': 'Remove' } },
    { id: 't-button-request-affiliation', key: 'button.request-affiliation', values: { 'pt-br': 'Solicitar filiação', 'en-us': 'Request affiliation' } },
    { id: 't-button-reset-demo', key: 'button.reset-demo', values: { 'pt-br': 'Reiniciar demonstração', 'en-us': 'Reset demo' } },
    { id: 't-button-set-default', key: 'button.set-default', values: { 'pt-br': 'Definir como padrão', 'en-us': 'Set default' } },
    { id: 't-button-sign-in', key: 'button.sign-in', values: { 'pt-br': 'Entrar', 'en-us': 'Sign in' } },
    { id: 't-button-sign-petition', key: 'button.sign-petition', values: { 'pt-br': 'Assinar petição', 'en-us': 'Sign petition' } },
    { id: 't-button-simulate-next-step', key: 'button.simulate-next-step', values: { 'pt-br': 'Simular próxima etapa', 'en-us': 'Simulate next step' } },
    { id: 't-button-start-conversation', key: 'button.start-conversation', values: { 'pt-br': 'Iniciar conversa', 'en-us': 'Start conversation' } },
    { id: 't-button-start-group', key: 'button.start-group', values: { 'pt-br': 'Criar grupo', 'en-us': 'Start group' } },
    { id: 't-button-unlink', key: 'button.unlink', values: { 'pt-br': 'Desvincular', 'en-us': 'Unlink' } },
    { id: 't-button-view-all-active-bills', key: 'button.view-all-active-bills', values: { 'pt-br': 'Ver Todos os Projetos Ativos', 'en-us': 'View All Active Bills' } },
    { id: 't-button-view-party', key: 'button.view-party', values: { 'pt-br': 'Ver partido', 'en-us': 'View party' } },
    { id: 't-category-humanitarian', key: 'category.humanitarian', values: { 'pt-br': 'Ajuda humanitária', 'en-us': 'Humanitarian aid' } },
    { id: 't-category-party', key: 'category.party', values: { 'pt-br': 'Iniciativa partidária', 'en-us': 'Party initiative' } },
    { id: 't-category-social', key: 'category.social', values: { 'pt-br': 'Causa social', 'en-us': 'Social cause' } },
    { id: 't-empty-approved-laws', key: 'empty.approved-laws', values: { 'pt-br': 'Nenhuma lei aprovada ainda', 'en-us': 'No approved laws yet' } },
    { id: 't-empty-bills', key: 'empty.bills', values: { 'pt-br': 'Nenhum projeto apresentado', 'en-us': 'No bills presented' } },
    { id: 't-empty-cpis', key: 'empty.cpis', values: { 'pt-br': 'Nenhum inquérito', 'en-us': 'No inquiries' } },
    { id: 't-empty-members-title', key: 'empty.members.title', values: { 'pt-br': 'Nenhum membro encontrado', 'en-us': 'No members found' } },
    { id: 't-empty-no-alerts-desc', key: 'empty.no-alerts.desc', values: { 'pt-br': 'Você está em dia.', 'en-us': 'You are all caught up.' } },
    { id: 't-empty-no-alerts-title', key: 'empty.no-alerts.title', values: { 'pt-br': 'Nenhum alerta', 'en-us': 'No alerts' } },
    { id: 't-empty-no-candidates-desc', key: 'empty.no-candidates.desc', values: { 'pt-br': 'Todos os políticos deste partido já estão vinculados.', 'en-us': 'Every politician from this party is already linked.' } },
    { id: 't-empty-no-candidates-title', key: 'empty.no-candidates.title', values: { 'pt-br': 'Nenhum candidato', 'en-us': 'No candidates' } },
    { id: 't-empty-no-countries-desc', key: 'empty.no-countries.desc', values: { 'pt-br': 'Adicione um país para começar a cadastrar seus estados.', 'en-us': 'Add a country to start registering its states.' } },
    { id: 't-empty-no-countries-title', key: 'empty.no-countries.title', values: { 'pt-br': 'Nenhum país', 'en-us': 'No countries' } },
    { id: 't-empty-no-elections-title', key: 'empty.no-elections.title', values: { 'pt-br': 'Nenhuma eleição encontrada', 'en-us': 'No elections found' } },
    { id: 't-empty-no-matches', key: 'empty.no-matches', values: { 'pt-br': 'Nenhum resultado encontrado.', 'en-us': 'No matches found.' } },
    { id: 't-empty-no-parties-match', key: 'empty.no-parties-match', values: { 'pt-br': 'Nenhum partido corresponde aos seus filtros.', 'en-us': 'No parties match your filters.' } },
    { id: 't-empty-no-politicians-linked-desc', key: 'empty.no-politicians-linked.desc', values: { 'pt-br': 'Vincule políticos a partir do diretório abaixo.', 'en-us': 'Link politicians from the directory below.' } },
    { id: 't-empty-no-politicians-linked-title', key: 'empty.no-politicians-linked.title', values: { 'pt-br': 'Nenhum político vinculado', 'en-us': 'No politicians linked' } },
    { id: 't-empty-no-politicians-match', key: 'empty.no-politicians-match', values: { 'pt-br': 'Nenhum político corresponde aos seus filtros.', 'en-us': 'No politicians match your filters.' } },
    { id: 't-empty-no-posts-filter', key: 'empty.no-posts-filter', values: { 'pt-br': 'Nenhuma publicação para este filtro.', 'en-us': 'No posts to show for this filter.' } },
    { id: 't-empty-no-states-desc', key: 'empty.no-states.desc', values: { 'pt-br': 'Os estados que você adicionar aparecerão aqui.', 'en-us': 'States you add will appear here.' } },
    { id: 't-empty-no-states-title', key: 'empty.no-states.title', values: { 'pt-br': 'Nenhum estado', 'en-us': 'No states' } },
    { id: 't-empty-no-translation-keys-desc', key: 'empty.no-translation-keys.desc', values: { 'pt-br': 'Adicione uma chave para começar a traduzir a plataforma.', 'en-us': 'Add a key to start translating the platform.' } },
    { id: 't-empty-no-translation-keys-title', key: 'empty.no-translation-keys.title', values: { 'pt-br': 'Nenhuma chave de tradução', 'en-us': 'No translation keys' } },
    { id: 't-empty-party-activity-desc', key: 'empty.party-activity.desc', values: { 'pt-br': 'As publicações deste partido aparecerão aqui.', 'en-us': 'Posts from this party will appear here.' } },
    { id: 't-empty-party-activity-title', key: 'empty.party-activity.title', values: { 'pt-br': 'Nenhuma publicação ainda', 'en-us': 'No publications yet' } },
    { id: 't-empty-pecs', key: 'empty.pecs', values: { 'pt-br': 'Nenhuma PEC', 'en-us': 'No PECs' } },
    { id: 't-empty-profile-activity-desc', key: 'empty.profile-activity.desc', values: { 'pt-br': 'As publicações deste político aparecerão aqui.', 'en-us': 'Posts from this politician will appear here.' } },
    { id: 't-empty-profile-activity-title', key: 'empty.profile-activity.title', values: { 'pt-br': 'Nenhuma publicação ainda', 'en-us': 'No publications yet' } },
    { id: 't-empty-rejected-laws', key: 'empty.rejected-laws', values: { 'pt-br': 'Nenhuma lei rejeitada', 'en-us': 'No rejected laws' } },
    { id: 't-empty-requests-desc', key: 'empty.requests.desc', values: { 'pt-br': 'Novas solicitações de filiação aparecerão aqui.', 'en-us': 'New affiliation requests will appear here.' } },
    { id: 't-empty-requests-title', key: 'empty.requests.title', values: { 'pt-br': 'Nenhuma solicitação', 'en-us': 'No requests' } },
    { id: 't-empty-try-different-scope', key: 'empty.try-different-scope', values: { 'pt-br': 'Tente um filtro de abrangência diferente.', 'en-us': 'Try a different scope filter.' } },
    { id: 't-empty-try-different-search', key: 'empty.try-different-search', values: { 'pt-br': 'Tente um termo de busca diferente.', 'en-us': 'Try a different search term.' } },
    { id: 't-error-enter-email-password', key: 'error.enter-email-password', values: { 'pt-br': 'Informe seu email e senha para continuar.', 'en-us': 'Enter your email and password to continue.' } },
    { id: 't-error-fill-name-email-password', key: 'error.fill-name-email-password', values: { 'pt-br': 'Preencha nome, email e senha para criar uma conta.', 'en-us': 'Fill in your name, email and password to create an account.' } },
    { id: 't-error-invalid-cnpj', key: 'error.invalid-cnpj', values: { 'pt-br': 'Informe um CNPJ válido (14 dígitos).', 'en-us': 'Enter a valid CNPJ (14 digits).' } },
    { id: 't-error-invalid-cpf', key: 'error.invalid-cpf', values: { 'pt-br': 'Informe um CPF válido (11 dígitos).', 'en-us': 'Enter a valid CPF (11 digits).' } },
    { id: 't-error-register-politician-required', key: 'error.register-politician-required', values: { 'pt-br': 'Preencha nome, cargo e estado para cadastrar um político.', 'en-us': 'Fill in the name, position and state to register a politician.' } },
    { id: 't-field-acronym', key: 'field.acronym', values: { 'pt-br': 'Sigla', 'en-us': 'Acronym' } },
    { id: 't-field-category', key: 'field.category', values: { 'pt-br': 'Categoria', 'en-us': 'Category' } },
    { id: 't-field-cnpj', key: 'field.cnpj', values: { 'pt-br': 'CNPJ (gabinete/comitê)', 'en-us': 'CNPJ (gabinete/comitê)' } },
    { id: 't-field-code', key: 'field.code', values: { 'pt-br': 'Código', 'en-us': 'Code' } },
    { id: 't-field-cpf', key: 'field.cpf', values: { 'pt-br': 'CPF', 'en-us': 'CPF' } },
    { id: 't-field-deadline', key: 'field.deadline', values: { 'pt-br': 'Prazo', 'en-us': 'Deadline' } },
    { id: 't-field-description', key: 'field.description', values: { 'pt-br': 'Descrição', 'en-us': 'Description' } },
    { id: 't-field-email', key: 'field.email', values: { 'pt-br': 'Email', 'en-us': 'Email' } },
    { id: 't-field-full-name', key: 'field.full-name', values: { 'pt-br': 'Nome completo', 'en-us': 'Full name' } },
    { id: 't-field-goal-usd', key: 'field.goal-usd', values: { 'pt-br': 'Meta (R$)', 'en-us': 'Goal (USD)' } },
    { id: 't-field-group-name', key: 'field.group-name', values: { 'pt-br': 'Nome do grupo', 'en-us': 'Group name' } },
    { id: 't-field-handle', key: 'field.handle', values: { 'pt-br': 'Usuário', 'en-us': 'Handle' } },
    { id: 't-field-level', key: 'field.level', values: { 'pt-br': 'Nível', 'en-us': 'Level' } },
    { id: 't-field-name', key: 'field.name', values: { 'pt-br': 'Nome', 'en-us': 'Name' } },
    { id: 't-field-number', key: 'field.number', values: { 'pt-br': 'Número', 'en-us': 'Number' } },
    { id: 't-field-password', key: 'field.password', values: { 'pt-br': 'Senha', 'en-us': 'Password' } },
    { id: 't-field-position', key: 'field.position', values: { 'pt-br': 'Cargo', 'en-us': 'Position' } },
    { id: 't-field-state', key: 'field.state', values: { 'pt-br': 'Estado', 'en-us': 'State' } },
    { id: 't-field-title', key: 'field.title', values: { 'pt-br': 'Título', 'en-us': 'Title' } },
    { id: 't-hint-affiliation-intro', key: 'hint.affiliation-intro', values: { 'pt-br': 'Inicie sua solicitação de filiação abaixo. Você fará o pré-cadastro, enviará documentos e pagará a contribuição — mas a confirmação oficial depende do partido e da Justiça Eleitoral.', 'en-us': 'Start your affiliation request below. You\'ll pre-register, upload documents and pay the contribution — but the official confirmation depends on the party and the Electoral Justice.' } },
    { id: 't-hint-already-have-account', key: 'hint.already-have-account', values: { 'pt-br': 'Já tem uma conta?', 'en-us': 'Already have an account?' } },
    { id: 't-hint-assistant-disclaimer', key: 'hint.assistant-disclaimer', values: { 'pt-br': 'Explicações geradas por IA apenas para compreensão cívica. Podem estar incompletas ou incorretas e não constituem aconselhamento jurídico. Sempre consulte o texto oficial.', 'en-us': 'AI-generated explanations for civic understanding only. They can be incomplete or wrong and are not legal advice. Always check the official text.' } },
    { id: 't-hint-assistant-placeholder', key: 'hint.assistant-placeholder', values: { 'pt-br': 'Escolha um tópico acima e peça um resumo, uma explicação em linguagem simples ou o que mudaria se fosse aprovado.', 'en-us': 'Pick a topic above, then ask for a summary, a plain-language explanation, or what would change if it passed.' } },
    { id: 't-hint-confirm-delete', key: 'hint.confirm-delete', values: { 'pt-br': 'Isso apagará seus dados e encerrará sua sessão. Esta ação não pode ser desfeita.', 'en-us': 'This will erase your data and sign you out. This cannot be undone.' } },
    { id: 't-hint-consent-lead', key: 'hint.consent-lead', values: { 'pt-br': 'Escolha como seus dados pessoais são usados. Você pode retirar qualquer consentimento não essencial a qualquer momento.', 'en-us': 'Choose how your personal data is used. You can withdraw any non-essential consent at any time.' } },
    { id: 't-hint-contribution-disclaimer', key: 'hint.contribution-disclaimer', values: { 'pt-br': 'A contribuição é destinada ao partido. O CivicPulse atua apenas como plataforma de pagamento.', 'en-us': 'The contribution is destined to the party. CivicPulse acts only as the payment platform.' } },
    { id: 't-hint-delete-my-account-desc', key: 'hint.delete-my-account-desc', values: { 'pt-br': 'Apague seus dados pessoais (direito à eliminação).', 'en-us': 'Erase your personal data (right to erasure).' } },
    { id: 't-hint-export-my-data-desc', key: 'hint.export-my-data-desc', values: { 'pt-br': 'Baixe um snapshot em JSON (direito à portabilidade).', 'en-us': 'Download a JSON snapshot (right to portability).' } },
    { id: 't-hint-fundraising-disclaimer-rest', key: 'hint.fundraising-disclaimer-rest', values: { 'pt-br': 'Isto não é arrecadação de campanha eleitoral, que é regulada separadamente e deve usar sistemas oficiais. Todas as ações mostram um livro-razão público de progresso.', 'en-us': 'This is not electoral campaign fundraising, which is regulated separately and must use official systems. All actions show a public progress ledger.' } },
    { id: 't-hint-fundraising-disclaimer-strong', key: 'hint.fundraising-disclaimer-strong', values: { 'pt-br': 'Apenas para causas sociais e iniciativas partidárias', 'en-us': 'For social causes and party initiatives only' } },
    { id: 't-hint-languages', key: 'hint.languages', values: { 'pt-br': 'Gerencie os idiomas disponíveis na plataforma. Exatamente um deles é o padrão.', 'en-us': 'Manage the languages available on the platform. Exactly one is the default.' } },
    { id: 't-hint-no-account', key: 'hint.no-account', values: { 'pt-br': 'Não tem uma conta?', 'en-us': 'Do not have an account?' } },
    { id: 't-hint-no-pending-contributions', key: 'hint.no-pending-contributions', values: { 'pt-br': 'Você está em dia. Nenhuma contribuição pendente.', 'en-us': 'You\'re all settled. No pending contributions.' } },
    { id: 't-hint-opinion-instruments', key: 'hint.opinion-instruments', values: { 'pt-br': 'Estes são instrumentos de opinião — medem o sentimento público e não são votos eleitorais oficiais nem pesquisas oficialmente registradas.', 'en-us': 'These are opinion instruments — they gauge public sentiment and are not official electoral votes or officially registered polls.' } },
    { id: 't-hint-picker-politicians-parties-only', key: 'hint.picker-politicians-parties-only', values: { 'pt-br': 'Apenas políticos e partidos podem ser adicionados — não cidadãos comuns.', 'en-us': 'Only politicians and parties can be added — not regular citizens.' } },
    { id: 't-hint-platform-admin', key: 'hint.platform-admin', values: { 'pt-br': 'Somente administradores da plataforma cadastram ou editam partidos, atribuem políticos a eles e gerenciam parâmetros compartilhados da plataforma. Políticos e cidadãos gerenciam seus próprios perfis em outro lugar.', 'en-us': 'Only platform administrators register or edit parties, assign politicians to them, and manage shared platform parameters. Politicians and citizens manage their own profiles elsewhere.' } },
    { id: 't-hint-policy-lead', key: 'hint.policy-lead', values: { 'pt-br': 'Leia nossos', 'en-us': 'Read our' } },
    { id: 't-hint-policy-tail', key: 'hint.policy-tail', values: { 'pt-br': 'para todos os detalhes de como os dados são coletados, armazenados (criptografados) e compartilhados.', 'en-us': 'for the full detail of how data is collected, stored (encrypted) and shared.' } },
    { id: 't-hint-politician-assignment', key: 'hint.politician-assignment', values: { 'pt-br': 'Atribua cada político a um partido, ou deixe-o independente.', 'en-us': 'Assign each politician to a party, or leave them independent.' } },
    { id: 't-hint-register-politician', key: 'hint.register-politician', values: { 'pt-br': 'Somente partidos podem cadastrar um político. Cadastrar aqui o vincula ao seu partido imediatamente.', 'en-us': 'Only parties can register a politician. Registering here links them to your party immediately.' } },
    { id: 't-hint-sign-in-to-contribute', key: 'hint.sign-in-to-contribute', values: { 'pt-br': 'Entre para contribuir.', 'en-us': 'Sign in to contribute.' } },
    { id: 't-hint-sign-in-to-participate', key: 'hint.sign-in-to-participate', values: { 'pt-br': 'Entre para participar.', 'en-us': 'Sign in to participate.' } },
    { id: 't-hint-stance-recorded', key: 'hint.stance-recorded', values: { 'pt-br': 'Sua posição foi registrada', 'en-us': 'Your stance was recorded' } },
    { id: 't-hint-thanks-for-voting', key: 'hint.thanks-for-voting', values: { 'pt-br': 'obrigado por votar', 'en-us': 'thanks for voting' } },
    { id: 't-hint-translation-tags', key: 'hint.translation-tags', values: { 'pt-br': 'Gerencie o texto de tradução de cada chave em todos os idiomas cadastrados.', 'en-us': 'Manage the translation string for each key in every registered language.' } },
    { id: 't-hint-your-data-lead', key: 'hint.your-data-lead', values: { 'pt-br': 'Exporte uma cópia portátil dos seus dados ou exclua sua conta permanentemente.', 'en-us': 'Export a portable copy of your data, or permanently delete your account.' } },
    { id: 't-label-absences', key: 'label.absences', values: { 'pt-br': 'Faltas', 'en-us': 'Absences' } },
    { id: 't-label-across-active-actions', key: 'label.across-active-actions', values: { 'pt-br': 'Nas ações ativas', 'en-us': 'Across active actions' } },
    { id: 't-label-active-actions', key: 'label.active-actions', values: { 'pt-br': 'Ações ativas', 'en-us': 'Active actions' } },
    { id: 't-label-active-levels', key: 'label.active-levels', values: { 'pt-br': 'Níveis ativos', 'en-us': 'Active levels' } },
    { id: 't-label-active-this-month', key: 'label.active-this-month', values: { 'pt-br': 'Ativos este mês', 'en-us': 'Active this month' } },
    { id: 't-label-affiliated-lower', key: 'label.affiliated-lower', values: { 'pt-br': 'filiados', 'en-us': 'affiliated' } },
    { id: 't-label-affiliation-requests', key: 'label.affiliation-requests', values: { 'pt-br': 'Solicitações de filiação', 'en-us': 'Affiliation requests' } },
    { id: 't-label-against', key: 'label.against', values: { 'pt-br': 'Contra', 'en-us': 'Against' } },
    { id: 't-label-ai-generated', key: 'label.ai-generated', values: { 'pt-br': 'Gerado por IA', 'en-us': 'AI-generated' } },
    { id: 't-label-all-levels', key: 'label.all-levels', values: { 'pt-br': 'Todos os níveis', 'en-us': 'All levels' } },
    { id: 't-label-all-parties', key: 'label.all-parties', values: { 'pt-br': 'Todos os partidos', 'en-us': 'All parties' } },
    { id: 't-label-all-scopes', key: 'label.all-scopes', values: { 'pt-br': 'Todas as abrangências', 'en-us': 'All scopes' } },
    { id: 't-label-all-spectrums', key: 'label.all-spectrums', values: { 'pt-br': 'Todos os espectros', 'en-us': 'All spectrums' } },
    { id: 't-label-all-states', key: 'label.all-states', values: { 'pt-br': 'Todos os estados', 'en-us': 'All states' } },
    { id: 't-label-and', key: 'label.and', values: { 'pt-br': 'e', 'en-us': 'and' } },
    { id: 't-label-ask', key: 'label.ask', values: { 'pt-br': 'Perguntar', 'en-us': 'Ask' } },
    { id: 't-label-attendance', key: 'label.attendance', values: { 'pt-br': 'Presença', 'en-us': 'Attendance' } },
    { id: 't-label-candidates', key: 'label.candidates', values: { 'pt-br': 'Candidatos', 'en-us': 'Candidates' } },
    { id: 't-label-choose-topic', key: 'label.choose-topic', values: { 'pt-br': 'Escolha um tópico', 'en-us': 'Choose a topic' } },
    { id: 't-label-closest-upcoming-date', key: 'label.closest-upcoming-date', values: { 'pt-br': 'Data mais próxima', 'en-us': 'Closest upcoming date' } },
    { id: 't-label-collected-this-month', key: 'label.collected-this-month', values: { 'pt-br': 'Arrecadado este mês', 'en-us': 'Collected this month' } },
    { id: 't-label-contributions', key: 'label.contributions', values: { 'pt-br': 'Contribuições', 'en-us': 'Contributions' } },
    { id: 't-label-country', key: 'label.country', values: { 'pt-br': 'País', 'en-us': 'Country' } },
    { id: 't-label-current', key: 'label.current', values: { 'pt-br': 'Atual', 'en-us': 'Current' } },
    { id: 't-label-default', key: 'label.default', values: { 'pt-br': 'Padrão', 'en-us': 'Default' } },
    { id: 't-label-delete-my-account', key: 'label.delete-my-account', values: { 'pt-br': 'Excluir minha conta', 'en-us': 'Delete my account' } },
    { id: 't-label-directories', key: 'label.directories', values: { 'pt-br': 'Diretórios', 'en-us': 'Directories' } },
    { id: 't-label-due', key: 'label.due', values: { 'pt-br': 'Vencimento', 'en-us': 'Due' } },
    { id: 't-label-due-date', key: 'label.due-date', values: { 'pt-br': 'Data de vencimento', 'en-us': 'Due date' } },
    { id: 't-label-engagement', key: 'label.engagement', values: { 'pt-br': 'Engajamento', 'en-us': 'Engagement' } },
    { id: 't-label-export-my-data', key: 'label.export-my-data', values: { 'pt-br': 'Exportar meus dados', 'en-us': 'Export my data' } },
    { id: 't-label-floor-speeches', key: 'label.floor-speeches', values: { 'pt-br': 'Discursos em plenário', 'en-us': 'Floor speeches' } },
    { id: 't-label-followers', key: 'label.followers', values: { 'pt-br': 'seguidores', 'en-us': 'followers' } },
    { id: 't-label-founded', key: 'label.founded', values: { 'pt-br': 'Fundação', 'en-us': 'Founded' } },
    { id: 't-label-founded-lower', key: 'label.founded-lower', values: { 'pt-br': 'fundado em', 'en-us': 'founded' } },
    { id: 't-label-goal', key: 'label.goal', values: { 'pt-br': 'Meta', 'en-us': 'Goal' } },
    { id: 't-label-group', key: 'label.group', values: { 'pt-br': 'Grupo', 'en-us': 'Group' } },
    { id: 't-label-ideology', key: 'label.ideology', values: { 'pt-br': 'Ideologia', 'en-us': 'Ideology' } },
    { id: 't-label-in-favor', key: 'label.in-favor', values: { 'pt-br': 'A favor', 'en-us': 'In favor' } },
    { id: 't-label-in-office', key: 'label.in-office', values: { 'pt-br': 'No cargo', 'en-us': 'In office' } },
    { id: 't-label-independent', key: 'label.independent', values: { 'pt-br': 'Independente', 'en-us': 'Independent' } },
    { id: 't-label-interviews', key: 'label.interviews', values: { 'pt-br': 'Entrevistas', 'en-us': 'Interviews' } },
    { id: 't-label-joined', key: 'label.joined', values: { 'pt-br': 'Entrou em', 'en-us': 'Joined' } },
    { id: 't-label-key', key: 'label.key', values: { 'pt-br': 'Chave', 'en-us': 'Key' } },
    { id: 't-label-keys', key: 'label.keys', values: { 'pt-br': 'chaves', 'en-us': 'keys' } },
    { id: 't-label-largest', key: 'label.largest', values: { 'pt-br': 'Maiores', 'en-us': 'Largest' } },
    { id: 't-label-leader', key: 'label.leader', values: { 'pt-br': 'Líder', 'en-us': 'Leader' } },
    { id: 't-label-ledger-pending', key: 'label.ledger-pending', values: { 'pt-br': 'Livro-razão pendente', 'en-us': 'Ledger pending' } },
    { id: 't-label-linked', key: 'label.linked', values: { 'pt-br': 'vinculados', 'en-us': 'linked' } },
    { id: 't-label-live', key: 'label.live', values: { 'pt-br': 'AO VIVO', 'en-us': 'LIVE' } },
    { id: 't-label-loading-more', key: 'label.loading-more', values: { 'pt-br': 'Carregando mais…', 'en-us': 'Loading more…' } },
    { id: 't-label-member', key: 'label.member', values: { 'pt-br': 'Membro', 'en-us': 'Member' } },
    { id: 't-label-member-id', key: 'label.member-id', values: { 'pt-br': 'ID do membro', 'en-us': 'Member ID' } },
    { id: 't-label-member-since', key: 'label.member-since', values: { 'pt-br': 'Membro desde', 'en-us': 'Member since' } },
    { id: 't-label-members', key: 'label.members', values: { 'pt-br': 'Membros', 'en-us': 'Members' } },
    { id: 't-label-members-lower', key: 'label.members-lower', values: { 'pt-br': 'membros', 'en-us': 'members' } },
    { id: 't-label-most-active', key: 'label.most-active', values: { 'pt-br': 'Mais ativos', 'en-us': 'Most active' } },
    { id: 't-label-most-followed', key: 'label.most-followed', values: { 'pt-br': 'Mais seguidos', 'en-us': 'Most followed' } },
    { id: 't-label-name-az', key: 'label.name-az', values: { 'pt-br': 'Nome (A–Z)', 'en-us': 'Name (A–Z)' } },
    { id: 't-label-nationwide', key: 'label.nationwide', values: { 'pt-br': 'Nacional', 'en-us': 'Nationwide' } },
    { id: 't-label-neutral', key: 'label.neutral', values: { 'pt-br': 'Neutro', 'en-us': 'Neutral' } },
    { id: 't-label-newest', key: 'label.newest', values: { 'pt-br': 'Mais recentes', 'en-us': 'Newest' } },
    { id: 't-label-next-election', key: 'label.next-election', values: { 'pt-br': 'Próxima eleição', 'en-us': 'Next election' } },
    { id: 't-label-of', key: 'label.of', values: { 'pt-br': 'de', 'en-us': 'of' } },
    { id: 't-label-official-trips', key: 'label.official-trips', values: { 'pt-br': 'Viagens oficiais', 'en-us': 'Official trips' } },
    { id: 't-label-open-for-contributions', key: 'label.open-for-contributions', values: { 'pt-br': 'Aberta para contribuições', 'en-us': 'Open for contributions' } },
    { id: 't-label-paid', key: 'label.paid', values: { 'pt-br': 'Pago', 'en-us': 'Paid' } },
    { id: 't-label-participants', key: 'label.participants', values: { 'pt-br': 'participantes', 'en-us': 'participants' } },
    { id: 't-label-party', key: 'label.party', values: { 'pt-br': 'Partido', 'en-us': 'Party' } },
    { id: 't-label-pending', key: 'label.pending', values: { 'pt-br': 'Pendente', 'en-us': 'Pending' } },
    { id: 't-label-pending-lower', key: 'label.pending-lower', values: { 'pt-br': 'pendentes', 'en-us': 'pending' } },
    { id: 't-label-people-who-contributed', key: 'label.people-who-contributed', values: { 'pt-br': 'Pessoas que contribuíram', 'en-us': 'People who contributed' } },
    { id: 't-label-plenary-caption', key: 'label.plenary-caption', values: { 'pt-br': 'Sessão plenária · Assembleia Legislativa', 'en-us': 'Plenary session · State Legislature' } },
    { id: 't-label-plenary-live', key: 'label.plenary-live', values: { 'pt-br': 'Sessão plenária — ao vivo agora', 'en-us': 'Plenary session — live now' } },
    { id: 't-label-president', key: 'label.president', values: { 'pt-br': 'Presidente', 'en-us': 'President' } },
    { id: 't-label-press-media', key: 'label.press-media', values: { 'pt-br': 'Imprensa e mídia', 'en-us': 'Press & media' } },
    { id: 't-label-privacy-policy', key: 'label.privacy-policy', values: { 'pt-br': 'Política de Privacidade', 'en-us': 'Privacy Policy' } },
    { id: 't-label-public-data', key: 'label.public-data', values: { 'pt-br': 'Dados públicos', 'en-us': 'Public data' } },
    { id: 't-label-public-ledger', key: 'label.public-ledger', values: { 'pt-br': 'Livro-razão público', 'en-us': 'Public ledger' } },
    { id: 't-label-recent-records', key: 'label.recent-records', values: { 'pt-br': 'Registros recentes', 'en-us': 'Recent records' } },
    { id: 't-label-recorded-absences', key: 'label.recorded-absences', values: { 'pt-br': 'Faltas registradas', 'en-us': 'Recorded absences' } },
    { id: 't-label-registered', key: 'label.registered', values: { 'pt-br': 'cadastrados', 'en-us': 'registered' } },
    { id: 't-label-representatives', key: 'label.representatives', values: { 'pt-br': 'Representantes', 'en-us': 'Representatives' } },
    { id: 't-label-responses', key: 'label.responses', values: { 'pt-br': 'respostas', 'en-us': 'responses' } },
    { id: 't-label-running-across-elections', key: 'label.running-across-elections', values: { 'pt-br': 'Concorrendo em todas as eleições', 'en-us': 'Running across all elections' } },
    { id: 't-label-scan-to-verify', key: 'label.scan-to-verify', values: { 'pt-br': 'Escaneie para verificar', 'en-us': 'Scan to verify' } },
    { id: 't-label-scheduled-elections', key: 'label.scheduled-elections', values: { 'pt-br': 'Eleições agendadas', 'en-us': 'Scheduled elections' } },
    { id: 't-label-serving-since', key: 'label.serving-since', values: { 'pt-br': 'No mandato desde', 'en-us': 'Serving since' } },
    { id: 't-label-sessions-present', key: 'label.sessions-present', values: { 'pt-br': 'sessões presentes', 'en-us': 'sessions present' } },
    { id: 't-label-signatures', key: 'label.signatures', values: { 'pt-br': 'assinaturas', 'en-us': 'signatures' } },
    { id: 't-label-signed', key: 'label.signed', values: { 'pt-br': 'Assinado', 'en-us': 'Signed' } },
    { id: 't-label-sort-by', key: 'label.sort-by', values: { 'pt-br': 'Ordenar por:', 'en-us': 'Sort by:' } },
    { id: 't-label-sort-following', key: 'label.sort-following', values: { 'pt-br': 'Seguindo', 'en-us': 'Following' } },
    { id: 't-label-sort-latest', key: 'label.sort-latest', values: { 'pt-br': 'Mais recentes', 'en-us': 'Latest' } },
    { id: 't-label-sort-top', key: 'label.sort-top', values: { 'pt-br': 'Mais relevantes', 'en-us': 'Top' } },
    { id: 't-label-speeches', key: 'label.speeches', values: { 'pt-br': 'Discursos', 'en-us': 'Speeches' } },
    { id: 't-label-supporters', key: 'label.supporters', values: { 'pt-br': 'Apoiadores', 'en-us': 'Supporters' } },
    { id: 't-label-supporters-lower', key: 'label.supporters-lower', values: { 'pt-br': 'apoiadores', 'en-us': 'supporters' } },
    { id: 't-label-terms-of-use', key: 'label.terms-of-use', values: { 'pt-br': 'Termos de Uso', 'en-us': 'Terms of Use' } },
    { id: 't-label-this-term', key: 'label.this-term', values: { 'pt-br': 'Este mandato', 'en-us': 'This term' } },
    { id: 't-label-total-affiliated', key: 'label.total-affiliated', values: { 'pt-br': 'Total de filiados', 'en-us': 'Total affiliated' } },
    { id: 't-label-total-raised', key: 'label.total-raised', values: { 'pt-br': 'Total arrecadado', 'en-us': 'Total raised' } },
    { id: 't-label-total-reported-expenses', key: 'label.total-reported-expenses', values: { 'pt-br': 'Total de despesas declaradas', 'en-us': 'Total reported expenses' } },
    { id: 't-label-until', key: 'label.until', values: { 'pt-br': 'até', 'en-us': 'until' } },
    { id: 't-label-upcoming', key: 'label.upcoming', values: { 'pt-br': 'Próximas', 'en-us': 'Upcoming' } },
    { id: 't-label-votes', key: 'label.votes', values: { 'pt-br': 'Votos', 'en-us': 'Votes' } },
    { id: 't-label-watching', key: 'label.watching', values: { 'pt-br': 'assistindo', 'en-us': 'watching' } },
    { id: 't-label-years-of-history', key: 'label.years-of-history', values: { 'pt-br': 'Anos de história', 'en-us': 'Years of history' } },
    { id: 't-level-federal', key: 'level.federal', values: { 'pt-br': 'Federal', 'en-us': 'Federal' } },
    { id: 't-level-municipal', key: 'level.municipal', values: { 'pt-br': 'Municipal', 'en-us': 'Municipal' } },
    { id: 't-level-state', key: 'level.state', values: { 'pt-br': 'Estadual', 'en-us': 'State' } },
    { id: 't-page-login-subtitle', key: 'page.login.subtitle', values: { 'pt-br': 'Entre para seguir políticos, partidos e participar da vida cívica.', 'en-us': 'Sign in to follow politicians, parties and take part in civic life.' } },
    { id: 't-page-login-title', key: 'page.login.title', values: { 'pt-br': 'Bem-vindo de volta', 'en-us': 'Welcome back' } },
    { id: 't-page-messages-title', key: 'page.messages.title', values: { 'pt-br': 'Mensagens', 'en-us': 'Messages' } },
    { id: 't-page-register-subtitle', key: 'page.register.subtitle', values: { 'pt-br': 'Cadastre-se como cidadão para seguir políticos, partidos e participar da vida cívica.', 'en-us': 'Join as a citizen to follow politicians, parties and take part in civic life.' } },
    { id: 't-page-register-title', key: 'page.register.title', values: { 'pt-br': 'Crie sua conta', 'en-us': 'Create your account' } },
    { id: 't-placeholder-search-name-city', key: 'placeholder.search-name-city', values: { 'pt-br': 'Buscar por nome ou cidade…', 'en-us': 'Search by name or city…' } },
    { id: 't-placeholder-search-parties', key: 'placeholder.search-parties', values: { 'pt-br': 'Buscar por nome, sigla ou ideologia…', 'en-us': 'Search by name, acronym or ideology…' } },
    { id: 't-placeholder-search-politicians', key: 'placeholder.search-politicians', values: { 'pt-br': 'Buscar por nome, usuário ou partido…', 'en-us': 'Search by name, handle or party…' } },
    { id: 't-placeholder-search-politicians-parties', key: 'placeholder.search-politicians-parties', values: { 'pt-br': 'Buscar políticos ou partidos…', 'en-us': 'Search politicians or parties…' } },
    { id: 't-placeholder-write-message', key: 'placeholder.write-message', values: { 'pt-br': 'Escreva uma mensagem…', 'en-us': 'Write a message…' } },
    { id: 't-section-affiliation-requests', key: 'section.affiliation-requests', values: { 'pt-br': 'Solicitações de filiação', 'en-us': 'Affiliation requests' } },
    { id: 't-section-affiliation-status', key: 'section.affiliation-status', values: { 'pt-br': 'Status da filiação', 'en-us': 'Affiliation status' } },
    { id: 't-section-approved-laws', key: 'section.approved-laws', values: { 'pt-br': 'Leis aprovadas', 'en-us': 'Approved laws' } },
    { id: 't-section-bills-presented', key: 'section.bills-presented', values: { 'pt-br': 'Projetos apresentados', 'en-us': 'Bills presented' } },
    { id: 't-section-career-timeline', key: 'section.career-timeline', values: { 'pt-br': 'Linha do tempo da carreira', 'en-us': 'Career timeline' } },
    { id: 't-section-civic-dossier', key: 'section.civic-dossier', values: { 'pt-br': 'Dossiê cívico', 'en-us': 'Civic dossier' } },
    { id: 't-section-committees', key: 'section.committees', values: { 'pt-br': 'Comissões, frentes e inquéritos', 'en-us': 'Committees, fronts & inquiries' } },
    { id: 't-section-consent', key: 'section.consent', values: { 'pt-br': 'Consentimento', 'en-us': 'Consent' } },
    { id: 't-section-countries', key: 'section.countries', values: { 'pt-br': 'Países', 'en-us': 'Countries' } },
    { id: 't-section-cpis', key: 'section.cpis', values: { 'pt-br': 'CPIs', 'en-us': 'CPIs' } },
    { id: 't-section-directories', key: 'section.directories', values: { 'pt-br': 'Diretórios', 'en-us': 'Directories' } },
    { id: 't-section-engagement-rate-week', key: 'section.engagement-rate-week', values: { 'pt-br': 'Taxa de engajamento — esta semana', 'en-us': 'Engagement rate — this week' } },
    { id: 't-section-events-agenda', key: 'section.events-agenda', values: { 'pt-br': 'Eventos, cursos e agenda', 'en-us': 'Events, courses & agenda' } },
    { id: 't-section-expense-breakdown', key: 'section.expense-breakdown', values: { 'pt-br': 'Detalhamento de despesas', 'en-us': 'Expense breakdown' } },
    { id: 't-section-history', key: 'section.history', values: { 'pt-br': 'História', 'en-us': 'History' } },
    { id: 't-section-interactions-by-content-type', key: 'section.interactions-by-content-type', values: { 'pt-br': 'Interações por tipo de conteúdo', 'en-us': 'Interactions by content type' } },
    { id: 't-section-internationalization', key: 'section.internationalization', values: { 'pt-br': 'Internacionalização', 'en-us': 'Internationalization' } },
    { id: 't-section-lgpd-rights', key: 'section.lgpd-rights', values: { 'pt-br': 'Seus direitos segundo a LGPD', 'en-us': 'Your rights under the LGPD' } },
    { id: 't-section-link-politician', key: 'section.link-politician', values: { 'pt-br': 'Vincular um político', 'en-us': 'Link a politician' } },
    { id: 't-section-linked-politicians', key: 'section.linked-politicians', values: { 'pt-br': 'Políticos vinculados', 'en-us': 'Linked politicians' } },
    { id: 't-section-mandates', key: 'section.mandates', values: { 'pt-br': 'Mandatos', 'en-us': 'Mandates' } },
    { id: 't-section-monthly-contribution', key: 'section.monthly-contribution', values: { 'pt-br': 'Contribuição mensal', 'en-us': 'Monthly contribution' } },
    { id: 't-section-new-conversation', key: 'section.new-conversation', values: { 'pt-br': 'Nova conversa', 'en-us': 'New conversation' } },
    { id: 't-section-new-fundraising-action', key: 'section.new-fundraising-action', values: { 'pt-br': 'Nova ação de arrecadação', 'en-us': 'New fundraising action' } },
    { id: 't-section-payment-history', key: 'section.payment-history', values: { 'pt-br': 'Histórico de pagamentos', 'en-us': 'Payment history' } },
    { id: 't-section-pecs', key: 'section.pecs', values: { 'pt-br': 'PECs', 'en-us': 'PECs' } },
    { id: 't-section-politician-assignment', key: 'section.politician-assignment', values: { 'pt-br': 'Atribuição de políticos', 'en-us': 'Politician assignment' } },
    { id: 't-section-program', key: 'section.program', values: { 'pt-br': 'Programa', 'en-us': 'Program' } },
    { id: 't-section-quick-actions', key: 'section.quick-actions', values: { 'pt-br': 'Ações rápidas', 'en-us': 'Quick actions' } },
    { id: 't-section-reach-by-district', key: 'section.reach-by-district', values: { 'pt-br': 'Alcance por região', 'en-us': 'Reach by district' } },
    { id: 't-section-recent-votes', key: 'section.recent-votes', values: { 'pt-br': 'Votos recentes', 'en-us': 'Recent votes' } },
    { id: 't-section-register-party', key: 'section.register-party', values: { 'pt-br': 'Cadastrar um partido', 'en-us': 'Register a party' } },
    { id: 't-section-register-politician', key: 'section.register-politician', values: { 'pt-br': 'Cadastrar um novo político', 'en-us': 'Register a new politician' } },
    { id: 't-section-registered-parties', key: 'section.registered-parties', values: { 'pt-br': 'Partidos cadastrados', 'en-us': 'Registered parties' } },
    { id: 't-section-rejected-laws', key: 'section.rejected-laws', values: { 'pt-br': 'Leis rejeitadas', 'en-us': 'Rejected laws' } },
    { id: 't-section-relevant-bills', key: 'section.relevant-bills', values: { 'pt-br': 'Projetos Relevantes', 'en-us': 'Relevant Bills' } },
    { id: 't-section-representatives', key: 'section.representatives', values: { 'pt-br': 'Representantes', 'en-us': 'Representatives' } },
    { id: 't-section-social-networks', key: 'section.social-networks', values: { 'pt-br': 'Redes sociais', 'en-us': 'Social networks' } },
    { id: 't-section-states-provinces', key: 'section.states-provinces', values: { 'pt-br': 'Estados e províncias', 'en-us': 'States & provinces' } },
    { id: 't-section-team-advisors', key: 'section.team-advisors', values: { 'pt-br': 'Equipe e assessores', 'en-us': 'Team & advisors' } },
    { id: 't-section-translation-tags', key: 'section.translation-tags', values: { 'pt-br': 'Tags de tradução', 'en-us': 'Translation tags' } },
    { id: 't-section-trending-topics', key: 'section.trending-topics', values: { 'pt-br': 'Tópicos em Alta', 'en-us': 'Trending Topics' } },
    { id: 't-section-your-data', key: 'section.your-data', values: { 'pt-br': 'Seus dados', 'en-us': 'Your data' } },
    { id: 't-spectrum-center', key: 'spectrum.center', values: { 'pt-br': 'Centro', 'en-us': 'Center' } },
    { id: 't-spectrum-center-left', key: 'spectrum.center-left', values: { 'pt-br': 'Centro-esquerda', 'en-us': 'Center-left' } },
    { id: 't-spectrum-center-right', key: 'spectrum.center-right', values: { 'pt-br': 'Centro-direita', 'en-us': 'Center-right' } },
    { id: 't-spectrum-left', key: 'spectrum.left', values: { 'pt-br': 'Esquerda', 'en-us': 'Left' } },
    { id: 't-spectrum-right', key: 'spectrum.right', values: { 'pt-br': 'Direita', 'en-us': 'Right' } },
    { id: 't-status-approved', key: 'status.approved', values: { 'pt-br': 'Aprovado', 'en-us': 'Approved' } },
    { id: 't-status-overdue', key: 'status.overdue', values: { 'pt-br': 'Atrasado', 'en-us': 'Overdue' } },
    { id: 't-status-paid', key: 'status.paid', values: { 'pt-br': 'Pago', 'en-us': 'Paid' } },
    { id: 't-status-pending', key: 'status.pending', values: { 'pt-br': 'Pendente', 'en-us': 'Pending' } },
    { id: 't-status-rejected', key: 'status.rejected', values: { 'pt-br': 'Rejeitado', 'en-us': 'Rejected' } },
    { id: 't-step-affiliated-desc', key: 'step.affiliated.desc', values: { 'pt-br': 'Cadastro confirmado. Seu cartão de associado digital está ativo.', 'en-us': 'Registration confirmed. Your digital membership card is active.' } },
    { id: 't-step-affiliated-label', key: 'step.affiliated.label', values: { 'pt-br': 'Oficialmente filiado', 'en-us': 'Officially affiliated' } },
    { id: 't-step-electoral-justice-desc', key: 'step.electoral-justice.desc', values: { 'pt-br': 'O partido protocolou sua filiação junto à Justiça Eleitoral.', 'en-us': 'The party filed your affiliation with the Electoral Justice.' } },
    { id: 't-step-electoral-justice-label', key: 'step.electoral-justice.label', values: { 'pt-br': 'Enviado à Justiça Eleitoral', 'en-us': 'Sent to Electoral Justice' } },
    { id: 't-step-party-approved-desc', key: 'step.party-approved.desc', values: { 'pt-br': 'Seu diretório local aprovou a solicitação de filiação.', 'en-us': 'Your local directory approved the affiliation request.' } },
    { id: 't-step-party-approved-label', key: 'step.party-approved.label', values: { 'pt-br': 'Partido aprovou', 'en-us': 'Party approved' } },
    { id: 't-step-requested-desc', key: 'step.requested.desc', values: { 'pt-br': 'Pré-cadastro e documentos enviados pelo aplicativo.', 'en-us': 'Pre-registration and documents submitted through the app.' } },
    { id: 't-step-requested-label', key: 'step.requested.label', values: { 'pt-br': 'Solicitação enviada', 'en-us': 'Request sent' } },
    { id: 't-step-under-review-desc', key: 'step.under-review.desc', values: { 'pt-br': 'O partido está validando seus documentos e contribuição.', 'en-us': 'The party is validating your documents and contribution.' } },
    { id: 't-step-under-review-label', key: 'step.under-review.label', values: { 'pt-br': 'Análise do partido', 'en-us': 'Party review' } },
    { id: 't-tab-activity', key: 'tab.activity', values: { 'pt-br': 'Atividade', 'en-us': 'Activity' } },
    { id: 't-tab-career-timeline', key: 'tab.career-timeline', values: { 'pt-br': 'Linha do Tempo', 'en-us': 'Career Timeline' } },
    { id: 't-tab-consultations', key: 'tab.consultations', values: { 'pt-br': 'Consultas', 'en-us': 'Consultations' } },
    { id: 't-tab-countries-states', key: 'tab.countries-states', values: { 'pt-br': 'Países e Estados', 'en-us': 'Countries & States' } },
    { id: 't-tab-languages', key: 'tab.languages', values: { 'pt-br': 'idiomas', 'en-us': 'languages' } },
    { id: 't-tab-members', key: 'tab.members', values: { 'pt-br': 'Membros', 'en-us': 'Members' } },
    { id: 't-tab-overview', key: 'tab.overview', values: { 'pt-br': 'Visão geral', 'en-us': 'Overview' } },
    { id: 't-tab-parliamentary', key: 'tab.parliamentary', values: { 'pt-br': 'Atividade Parlamentar', 'en-us': 'Parliamentary Activity' } },
    { id: 't-tab-parties-politicians', key: 'tab.parties-politicians', values: { 'pt-br': 'Partidos e Políticos', 'en-us': 'Parties & Politicians' } },
    { id: 't-tab-petitions', key: 'tab.petitions', values: { 'pt-br': 'Petições', 'en-us': 'Petitions' } },
    { id: 't-tab-politicians', key: 'tab.politicians', values: { 'pt-br': 'Políticos', 'en-us': 'Politicians' } },
    { id: 't-tab-requests', key: 'tab.requests', values: { 'pt-br': 'Solicitações', 'en-us': 'Requests' } },
    { id: 't-tab-surveys', key: 'tab.surveys', values: { 'pt-br': 'Pesquisas', 'en-us': 'Surveys' } },
    { id: 't-tab-transparency', key: 'tab.transparency', values: { 'pt-br': 'Transparência', 'en-us': 'Transparency' } },
    { id: 't-vote-absent', key: 'vote.absent', values: { 'pt-br': 'AUSENTE', 'en-us': 'ABSENT' } },
    { id: 't-vote-abstain', key: 'vote.abstain', values: { 'pt-br': 'ABSTENÇÃO', 'en-us': 'ABSTAIN' } },
    { id: 't-vote-no', key: 'vote.no', values: { 'pt-br': 'NÃO', 'en-us': 'NO' } },
    { id: 't-vote-yes', key: 'vote.yes', values: { 'pt-br': 'SIM', 'en-us': 'YES' } },

    // ---- Ternary-embedded translate keys missed by the bulk pass ----
    { id: 't-button-create-action', key: 'button.create-action', values: { 'pt-br': 'Criar ação', 'en-us': 'Create action' } },
    { id: 't-button-register-party', key: 'button.register-party', values: { 'pt-br': 'Cadastrar partido', 'en-us': 'Register party' } },
    { id: 't-button-reinstate', key: 'button.reinstate', values: { 'pt-br': 'Reintegrar', 'en-us': 'Reinstate' } },
    { id: 't-button-suspend', key: 'button.suspend', values: { 'pt-br': 'Suspender', 'en-us': 'Suspend' } },
    { id: 't-label-active-member', key: 'label.active-member', values: { 'pt-br': 'Membro ativo', 'en-us': 'Active member' } },
    { id: 't-label-parties', key: 'label.parties', values: { 'pt-br': 'partidos', 'en-us': 'parties' } },
    { id: 't-label-politician', key: 'label.politician', values: { 'pt-br': 'político', 'en-us': 'politician' } },
    { id: 't-label-politicians', key: 'label.politicians', values: { 'pt-br': 'políticos', 'en-us': 'politicians' } },

    // ---- Election detail page ----
    { id: 't-button-back-to-elections', key: 'button.back-to-elections', values: { 'pt-br': 'Voltar às Eleições', 'en-us': 'Back to Elections' } },
    { id: 't-label-concluded', key: 'label.concluded', values: { 'pt-br': 'Encerrada', 'en-us': 'Concluded' } },
    { id: 't-empty-no-candidates-for-election', key: 'empty.no-candidates-for-election', values: { 'pt-br': 'Nenhum candidato foi vinculado a esta eleição ainda.', 'en-us': 'No candidates have been linked to this election yet.' } },
    { id: 't-empty-election-not-found-title', key: 'empty.election-not-found.title', values: { 'pt-br': 'Eleição não encontrada', 'en-us': 'Election not found' } },
    { id: 't-empty-election-not-found-desc', key: 'empty.election-not-found.desc', values: { 'pt-br': 'Esta eleição pode ter sido removida ou o link está incorreto.', 'en-us': 'This election may have been removed or the link is incorrect.' } },

    // ---- CPF now mandatory on citizen self-registration ----
    { id: 't-error-fill-name-email-cpf-password', key: 'error.fill-name-email-cpf-password', values: { 'pt-br': 'Preencha nome, email, CPF e senha para criar uma conta.', 'en-us': 'Fill in your name, email, CPF and password to create an account.' } },
  ]);
  readonly translations = this._translations.asReadonly();

  addTranslation(key: string): void {
    const trimmed = key.trim();
    if (!trimmed || this._translations().some((t) => t.key === trimmed)) return;
    const values: Record<string, string> = {};
    this._languages().forEach((l) => (values[l.id] = ''));
    this._translations.update((list) => [...list, { id: `t-${this.slugify(trimmed)}-${Date.now()}`, key: trimmed, values }]);
  }

  updateTranslationValue(id: string, languageId: string, value: string): void {
    this._translations.update((list) =>
      list.map((t) => (t.id === id ? { ...t, values: { ...t.values, [languageId]: value } } : t)),
    );
  }

  removeTranslation(id: string): void {
    this._translations.update((list) => list.filter((t) => t.id !== id));
  }

  private slugify(value: string): string {
    return value.trim().toLowerCase().replace(/[^a-z0-9]+/g, '-') || `id-${Date.now()}`;
  }
}
