/** Official affiliation lifecycle — mirrors the electoral-law flow. */
export type FiliationStatus =
  | 'not-started'
  | 'requested' // solicitação enviada
  | 'under-review' // partido analisando documentos
  | 'party-approved' // partido aprovou
  | 'electoral-justice' // enviado à Justiça Eleitoral
  | 'affiliated'; // filiado oficialmente

export interface FiliationStep {
  readonly status: FiliationStatus;
  readonly label: string;
  readonly description: string;
  readonly icon: string;
}

export type PaymentStatus = 'paid' | 'pending' | 'overdue';

export interface MembershipFee {
  readonly id: string;
  readonly reference: string; // e.g. "Julho/2026"
  readonly amount: number;
  readonly dueDate: string;
  status: PaymentStatus;
  readonly paidAt?: string;
}

export interface DigitalCard {
  readonly memberId: string;
  readonly holderName: string;
  readonly party: string;
  readonly partyAcronym: string;
  readonly since: string;
  readonly qrPayload: string;
}
