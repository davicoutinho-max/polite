import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, switchMap } from 'rxjs';
import {
  LEGISLATIVE_BILL_TYPES,
  LegislativeBillDetail,
  LegislativeBillSummary,
  LegislativeSource,
  LegislativeTimelineEntry,
  LegislativeVotingRecord,
} from '../models';
import { DirectoryService } from './directory.service';

const CAMARA_BASE = 'https://dadosabertos.camara.leg.br/api/v2';
const SENADO_BASE = 'https://legis.senado.leg.br/dadosabertos';
const JSON_HEADERS = { Accept: 'application/json' };

interface CamaraProposicaoDto {
  readonly id: number;
  readonly siglaTipo: string;
  readonly numero: number;
  readonly ano: number;
  readonly ementa: string;
  readonly dataApresentacao: string | null;
}

interface CamaraSearchResponse {
  readonly dados: CamaraProposicaoDto[];
}

interface CamaraTramitacaoDto {
  readonly dataHora: string | null;
  readonly descricaoTramitacao: string | null;
  readonly despacho: string | null;
  readonly siglaOrgao: string | null;
}

interface CamaraTramitacoesResponse {
  readonly dados: CamaraTramitacaoDto[];
}

interface SenadoMateriaDto {
  readonly Codigo: string;
  readonly DescricaoIdentificacao: string;
  readonly Sigla: string;
  readonly Ementa: string;
  readonly Data: string | null;
}

interface SenadoSearchResponse {
  readonly PesquisaBasicaMateria?: {
    readonly Materias?: {
      readonly Materia?: SenadoMateriaDto[] | SenadoMateriaDto;
    };
  };
}

interface CamaraProposicaoDetailDto {
  readonly id: number;
  readonly siglaTipo: string;
  readonly numero: number;
  readonly ano: number;
  readonly ementa: string;
  readonly ementaDetalhada: string | null;
  readonly keywords: string | null;
  readonly dataApresentacao: string | null;
  readonly urlInteiroTeor: string | null;
  readonly statusProposicao: {
    readonly dataHora: string | null;
    readonly descricaoTramitacao: string | null;
    readonly despacho: string | null;
    readonly siglaOrgao: string | null;
  } | null;
}

interface CamaraProposicaoDetailResponse {
  readonly dados: CamaraProposicaoDetailDto;
}

interface CamaraAutorDto {
  readonly nome: string;
}

interface CamaraAutoresResponse {
  readonly dados: CamaraAutorDto[];
}

interface CamaraVotacaoDto {
  readonly id: string;
  readonly data: string;
  readonly descricao: string;
  readonly aprovacao: number | null;
  readonly siglaOrgao: string | null;
}

interface CamaraVotacoesResponse {
  readonly dados: CamaraVotacaoDto[];
}

interface CamaraVotoDto {
  readonly tipoVoto: string;
}

interface CamaraVotosResponse {
  readonly dados: CamaraVotoDto[];
}

interface SenadoDetalheMateriaResponse {
  readonly DetalheMateria?: {
    readonly Materia?: {
      readonly DadosBasicosMateria?: {
        readonly EmentaMateria?: string;
        readonly Autor?: string;
        readonly IndexacaoMateria?: string;
      };
    };
  };
}

interface SenadoInformeLegislativoDto {
  readonly Data: string;
  readonly Descricao: string;
  readonly Local?: { readonly NomeLocal?: string };
}

interface SenadoMovimentacaoResponse {
  readonly MovimentacaoMateria?: {
    readonly Materia?: {
      readonly Autuacoes?: {
        readonly Autuacao?:
          | {
              readonly InformesLegislativos?: {
                readonly InformeLegislativo?: SenadoInformeLegislativoDto[] | SenadoInformeLegislativoDto;
              };
            }[]
          | {
              readonly InformesLegislativos?: {
                readonly InformeLegislativo?: SenadoInformeLegislativoDto[] | SenadoInformeLegislativoDto;
              };
            };
      };
    };
  };
}

/** Wraps a single value or array from Senado's XML-shaped JSON (a lone child collapses to an
 * object instead of a 1-item array) into a normalized array. */
function asArray<T>(value: T[] | T | undefined): T[] {
  if (value === undefined) {
    return [];
  }
  return Array.isArray(value) ? value : [value];
}

/** Real bills (Projetos de Lei) pulled live from Câmara dos Deputados and Senado Federal's own
 * open-data APIs — not internal CivicPulse data, and not LLM-generated. Both APIs expose open CORS
 * (`Access-Control-Allow-Origin: *`) and a JSON representation for the endpoints used here, so
 * this calls them directly from the browser with no backend proxy — except author party, see
 * resolveAuthorParty's javadoc for the one confirmed exception. */
@Injectable({ providedIn: 'root' })
export class LegislativeOpenDataService {
  private readonly http = inject(HttpClient);
  private readonly directory = inject(DirectoryService);

  /** How many merged/sorted results a single "page" holds — `page` is 1-based and cumulative
   * (page 2 returns the first two pages' worth, not just the second slice), since Câmara's own
   * `itens` param behaves as a stable prefix count and Senado's search has no server-side
   * pagination at all (it always returns its whole result set, sliced here instead). */
  private readonly PAGE_SIZE = 8;

  searchBills(keyword: string, page = 1): Observable<LegislativeBillSummary[]> {
    const term = keyword.trim();
    if (!term) {
      return of([]);
    }
    const itens = page * this.PAGE_SIZE;
    return forkJoin([this.fetchCamara({ keywords: term, itens, ordem: 'DESC', ordenarPor: 'id' }), this.fetchSenado({ palavraChave: term })]).pipe(
      map(([camara, senado]) => this.merge(camara, senado.slice(0, itens)).slice(0, itens)),
    );
  }

  /** Groups the current, real legislative agenda by bill type (see `LEGISLATIVE_BILL_TYPES`) so a
   * citizen sees what's actually in progress without having to search for anything first —
   * searching stays available, but is optional. */
  listRecentByType(page = 1): Observable<Record<string, LegislativeBillSummary[]>> {
    return forkJoin(LEGISLATIVE_BILL_TYPES.map((type) => this.listByType(type.code, page))).pipe(
      map((lists) => Object.fromEntries(LEGISLATIVE_BILL_TYPES.map((type, i) => [type.code, lists[i]]))),
    );
  }

  listByType(typeCode: string, page = 1): Observable<LegislativeBillSummary[]> {
    const itens = page * this.PAGE_SIZE;
    return forkJoin([
      this.fetchCamara({ siglaTipo: typeCode, itens, ordem: 'DESC', ordenarPor: 'id' }),
      this.fetchSenado({ sigla: typeCode }),
    ]).pipe(map(([camara, senado]) => this.merge(camara, senado.slice(0, itens)).slice(0, itens)));
  }

  private merge(camara: LegislativeBillSummary[], senado: LegislativeBillSummary[]): LegislativeBillSummary[] {
    return [...camara, ...senado].sort((a, b) => (b.presentedDate ?? '').localeCompare(a.presentedDate ?? ''));
  }

  getTimeline(source: LegislativeSource, id: string): Observable<LegislativeTimelineEntry[]> {
    return source === 'camara' ? this.getCamaraTimeline(id) : this.getSenadoTimeline(id);
  }

  private fetchCamara(params: Record<string, string | number>): Observable<LegislativeBillSummary[]> {
    return this.http.get<CamaraSearchResponse>(`${CAMARA_BASE}/proposicoes`, { headers: JSON_HEADERS, params }).pipe(
      map((res) =>
        (res.dados ?? []).map(
          (p): LegislativeBillSummary => ({
            source: 'camara' as LegislativeSource,
            id: String(p.id),
            identification: `${p.siglaTipo} ${p.numero}/${p.ano}`,
            typeLabel: p.siglaTipo,
            summary: p.ementa ?? '',
            presentedDate: p.dataApresentacao ? p.dataApresentacao.slice(0, 10) : null,
            officialUrl: `https://www.camara.leg.br/proposicoesWeb/fichadetramitacao?idProposicao=${p.id}`,
          }),
        ),
      ),
      catchError(() => of([])),
    );
  }

  private fetchSenado(params: Record<string, string | number>): Observable<LegislativeBillSummary[]> {
    return this.http.get<SenadoSearchResponse>(`${SENADO_BASE}/materia/pesquisa/lista`, { headers: JSON_HEADERS, params }).pipe(
      map((res) => {
        const materias = asArray(res.PesquisaBasicaMateria?.Materias?.Materia);
        // Senado's search has no server-side pagination — it always returns everything matching,
        // so this caps the payload; callers slice further down to the actual page size.
        return materias.slice(0, 50).map(
          (m): LegislativeBillSummary => ({
            source: 'senado' as LegislativeSource,
            id: m.Codigo,
            identification: m.DescricaoIdentificacao,
            typeLabel: m.Sigla,
            summary: m.Ementa ?? '',
            presentedDate: m.Data ?? null,
            officialUrl: `https://www25.senado.leg.br/web/atividade/materias/-/materia/${m.Codigo}`,
          }),
        );
      }),
      catchError(() => of([])),
    );
  }

  private getCamaraTimeline(proposicaoId: string): Observable<LegislativeTimelineEntry[]> {
    return this.http.get<CamaraTramitacoesResponse>(`${CAMARA_BASE}/proposicoes/${proposicaoId}/tramitacoes`, { headers: JSON_HEADERS }).pipe(
      map((res) =>
        (res.dados ?? [])
          .map(
            (t): LegislativeTimelineEntry => ({
              date: t.dataHora ?? '',
              description: t.despacho || t.descricaoTramitacao || '',
              location: t.siglaOrgao,
            }),
          )
          .sort((a, b) => b.date.localeCompare(a.date)),
      ),
      catchError(() => of([])),
    );
  }

  getBillDetail(source: LegislativeSource, id: string): Observable<LegislativeBillDetail | null> {
    return source === 'camara' ? this.getCamaraDetail(id) : this.getSenadoDetail(id);
  }

  getVotingRecords(source: LegislativeSource, id: string): Observable<LegislativeVotingRecord[]> {
    return source === 'camara' ? this.getCamaraVotingRecords(id) : of([]);
  }

  /** Same author lookup as getCamaraDetail (name + party), exposed standalone so the Bills list
   * page can enrich already-loaded cards without re-fetching the whole bill detail. Senado bills
   * resolve to nulls immediately — no per-bill call — since that API's list endpoint doesn't carry
   * an author field at all (only its separate per-bill detail endpoint does, see getSenadoDetail),
   * and its party has no separately-resolvable source either way (see
   * LegislativeBillDetail.authorParty's javadoc). */
  resolveAuthorInfo(source: LegislativeSource, id: string): Observable<{ readonly name: string | null; readonly party: string | null }> {
    if (source !== 'camara') {
      return of({ name: null, party: null });
    }
    return this.http.get<CamaraAutoresResponse>(`${CAMARA_BASE}/proposicoes/${id}/autores`, { headers: JSON_HEADERS }).pipe(
      map((res) => res.dados ?? []),
      catchError(() => of([])),
      map((autores) => ({ name: autores[0]?.nome ?? null, party: this.matchAuthorParty(autores[0]?.nome) })),
    );
  }

  /** Câmara's per-deputy detail endpoint (`/deputados/{id}`) — the only place with a deputy's
   * current party — confirmed to have NO CORS headers at all (unlike every `/proposicoes/**`
   * endpoint used elsewhere in this service), so it cannot be called from the browser. Matching
   * the author's name against DirectoryService's already-loaded, already-synced politicians
   * (same underlying Câmara "nome" field, populated by the exact same government sync) avoids the
   * problem entirely — zero extra network calls, and no CORS issue since nothing external is hit.
   * Only misses for authors who aren't currently-serving synced deputies (rare: departed members,
   * or an author record not yet in the directory), in which case this stays `null`. */
  private matchAuthorParty(authorName: string | undefined): string | null {
    if (!authorName) {
      return null;
    }
    const politician = this.directory.politicians().find((p) => p.name === authorName);
    if (!politician?.partyAcronym) {
      return null;
    }
    return politician.state ? `${politician.partyAcronym}-${politician.state}` : politician.partyAcronym;
  }

  private getCamaraDetail(id: string): Observable<LegislativeBillDetail | null> {
    return forkJoin([
      this.http.get<CamaraProposicaoDetailResponse>(`${CAMARA_BASE}/proposicoes/${id}`, { headers: JSON_HEADERS }).pipe(catchError(() => of(null))),
      this.http.get<CamaraAutoresResponse>(`${CAMARA_BASE}/proposicoes/${id}/autores`, { headers: JSON_HEADERS }).pipe(
        map((res) => res.dados ?? []),
        catchError(() => of([])),
      ),
    ]).pipe(
      map(([detailRes, autores]) => {
        if (!detailRes?.dados) {
          return null;
        }
        const authorParty = this.matchAuthorParty(autores[0]?.nome);
        const p = detailRes.dados;
        return {
          source: 'camara' as LegislativeSource,
          id: String(p.id),
          identification: `${p.siglaTipo} ${p.numero}/${p.ano}`,
          typeLabel: p.siglaTipo,
          summary: p.ementa ?? '',
          presentedDate: p.dataApresentacao ? p.dataApresentacao.slice(0, 10) : null,
          officialUrl: `https://www.camara.leg.br/proposicoesWeb/fichadetramitacao?idProposicao=${p.id}`,
          fullSummary: p.ementaDetalhada || p.ementa || null,
          author: autores.map((a) => a.nome).join(', ') || null,
          authorParty,
          currentStatusDescription: p.statusProposicao?.despacho || p.statusProposicao?.descricaoTramitacao || null,
          currentStatusDate: p.statusProposicao?.dataHora ? p.statusProposicao.dataHora.slice(0, 10) : null,
          currentStatusLocation: p.statusProposicao?.siglaOrgao ?? null,
          fullTextUrl: p.urlInteiroTeor ?? null,
          keywords: p.keywords ?? null,
        };
      }),
    );
  }

  private getSenadoDetail(codigoMateria: string): Observable<LegislativeBillDetail | null> {
    return forkJoin([
      this.http
        .get<SenadoDetalheMateriaResponse>(`${SENADO_BASE}/materia/${codigoMateria}`, { headers: JSON_HEADERS })
        .pipe(catchError(() => of(null))),
      this.getSenadoTimeline(codigoMateria),
    ]).pipe(
      map(([detailRes, timeline]) => {
        const dados = detailRes?.DetalheMateria?.Materia?.DadosBasicosMateria;
        const latest = timeline[0] ?? null;
        return {
          source: 'senado' as LegislativeSource,
          id: codigoMateria,
          identification: codigoMateria,
          typeLabel: '',
          summary: dados?.EmentaMateria ?? '',
          presentedDate: null,
          officialUrl: `https://www25.senado.leg.br/web/atividade/materias/-/materia/${codigoMateria}`,
          fullSummary: dados?.EmentaMateria ?? null,
          author: dados?.Autor ?? null,
          authorParty: null,
          currentStatusDescription: latest?.description ?? null,
          currentStatusDate: latest?.date ?? null,
          currentStatusLocation: latest?.location ?? null,
          fullTextUrl: null,
          keywords: dados?.IndexacaoMateria ?? null,
        };
      }),
    );
  }

  /** Fetches the tally for every votação in one shot via forkJoin — bills rarely accumulate more
   * than a couple dozen plenary votes over their lifetime, so this stays a handful of requests, a
   * reasonable cost for a detail page the citizen opened deliberately (unlike the list page, which
   * must stay cheap since it loads many bills at once). */
  private getCamaraVotingRecords(proposicaoId: string): Observable<LegislativeVotingRecord[]> {
    return this.http.get<CamaraVotacoesResponse>(`${CAMARA_BASE}/proposicoes/${proposicaoId}/votacoes`, { headers: JSON_HEADERS }).pipe(
      switchMap((res) => {
        const votacoes = res.dados ?? [];
        if (votacoes.length === 0) {
          return of([]);
        }
        return forkJoin(votacoes.map((v) => this.getCamaraVoteTally(v.id))).pipe(
          map((tallies) =>
            votacoes
              .map(
                (v, i): LegislativeVotingRecord => ({
                  id: v.id,
                  date: v.data,
                  description: v.descricao,
                  approved: v.aprovacao === null ? null : v.aprovacao === 1,
                  location: v.siglaOrgao,
                  tally: tallies[i],
                }),
              )
              .sort((a, b) => b.date.localeCompare(a.date)),
          ),
        );
      }),
      catchError(() => of([])),
    );
  }

  private getCamaraVoteTally(votacaoId: string): Observable<LegislativeVotingRecord['tally']> {
    return this.http.get<CamaraVotosResponse>(`${CAMARA_BASE}/votacoes/${votacaoId}/votos`, { headers: JSON_HEADERS }).pipe(
      map((res) => {
        const votos = res.dados ?? [];
        if (votos.length === 0) {
          return null;
        }
        const tally = { yes: 0, no: 0, abstain: 0, absent: 0 };
        for (const voto of votos) {
          const tipo = (voto.tipoVoto ?? '').toLowerCase();
          if (tipo.includes('sim')) tally.yes++;
          else if (tipo.includes('não') || tipo.includes('nao')) tally.no++;
          else if (tipo.includes('absten')) tally.abstain++;
          else tally.absent++;
        }
        return tally;
      }),
      catchError(() => of(null)),
    );
  }

  private getSenadoTimeline(codigoMateria: string): Observable<LegislativeTimelineEntry[]> {
    return this.http
      .get<SenadoMovimentacaoResponse>(`${SENADO_BASE}/materia/movimentacoes/${codigoMateria}`, { headers: JSON_HEADERS })
      .pipe(
        map((res) => {
          const autuacoes = asArray(res.MovimentacaoMateria?.Materia?.Autuacoes?.Autuacao);
          const entries = autuacoes.flatMap((a) => asArray(a.InformesLegislativos?.InformeLegislativo));
          return entries
            .map(
              (i): LegislativeTimelineEntry => ({
                date: i.Data,
                description: i.Descricao,
                location: i.Local?.NomeLocal ?? null,
              }),
            )
            .sort((a, b) => b.date.localeCompare(a.date));
        }),
        catchError(() => of([])),
      );
  }
}
