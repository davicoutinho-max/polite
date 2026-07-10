import { StatusTag } from './tag.model';

/** Abaixo-assinado / petition. */
export interface Petition {
  readonly id: string;
  readonly title: string;
  readonly summary: string;
  readonly category: string;
  readonly goal: number;
  signatures: number;
  readonly deadline: string;
  readonly status: StatusTag;
  signed: boolean;
}

/** Consulta pública — citizens register a stance. */
export type ConsultationStance = 'favor' | 'against' | 'neutral';

export interface Consultation {
  readonly id: string;
  readonly title: string;
  readonly description: string;
  readonly deadline: string;
  readonly status: StatusTag;
  responses: number;
  stance: ConsultationStance | null;
}

/** Pesquisa / poll option. */
export interface PollOption {
  readonly id: string;
  readonly label: string;
  votes: number;
}

export interface Survey {
  readonly id: string;
  readonly question: string;
  readonly context: string;
  options: PollOption[];
  votedOptionId: string | null;
}
