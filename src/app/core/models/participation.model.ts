import { StatusTag } from './tag.model';

/** "Apoio Verificado" — a lightweight identified-citizen endorsement. "Iniciativa Popular" —
 * aimed at petitions intending to meet the legal requirements for a formal presentation, which
 * additionally requires electoral data, an explicit e-signature consent and an identity-
 * validation step. */
export type PetitionType = 'verified_support' | 'popular_initiative';

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
  readonly imageUrl: string | null;
  readonly videoUrl: string | null;
  readonly fileUrl: string | null;
  readonly fileName: string | null;
  readonly petitionType: PetitionType;
}

/** Everything captured in the sign wizard's first step, before the code/identity check — a
 * superset of both tiers' requirements; fields a given tier doesn't use are left null. */
export interface StartPetitionSignatureCommand {
  readonly fullName: string;
  readonly cpf: string;
  readonly birthDate: string | null;
  readonly city: string | null;
  readonly state: string | null;
  readonly verificationMethod: 'sms' | 'email';
  readonly contact: string | null;
  readonly electoralData: string | null;
  readonly eSignatureConsent: boolean;
  readonly typedSignature: string;
}

export interface PetitionSignatureVerificationStarted {
  readonly verificationId: string;
  readonly demoCode: string;
  readonly contact: string | null;
  readonly method: string;
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
