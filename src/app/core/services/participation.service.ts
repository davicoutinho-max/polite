import { Injectable, signal } from '@angular/core';
import { Consultation, ConsultationStance, Petition, Survey } from '../models';

@Injectable({ providedIn: 'root' })
export class ParticipationService {
  private readonly _petitions = signal<Petition[]>([
    {
      id: 'pet1',
      title: 'Open ledger for all municipal contracts',
      summary: 'Require every city contract above R$ 10,000 to be published on a public digital ledger within 48 hours.',
      category: 'Transparency',
      goal: 50000,
      signatures: 38240,
      deadline: 'Closes Aug 30, 2026',
      status: { label: 'Open', severity: 'success' },
      signed: false,
    },
    {
      id: 'pet2',
      title: 'Expand accessible public transit',
      summary: 'Guarantee step-free access and audio guidance at every station across the metropolitan network.',
      category: 'Mobility',
      goal: 25000,
      signatures: 24680,
      deadline: 'Closes Jul 22, 2026',
      status: { label: 'Almost there', severity: 'warning' },
      signed: false,
    },
    {
      id: 'pet3',
      title: 'Protect the north watershed green belt',
      summary: 'Designate the north watershed as a permanent conservation area, blocking new construction permits.',
      category: 'Environment',
      goal: 30000,
      signatures: 30120,
      deadline: 'Goal reached',
      status: { label: 'Goal reached', severity: 'secondary' },
      signed: true,
    },
  ]);
  readonly petitions = this._petitions.asReadonly();

  private readonly _consultations = signal<Consultation[]>([
    {
      id: 'con1',
      title: 'Draft Fiscal Transparency Amendment (PEC 33)',
      description: 'Share your stance on the constitutional guarantee of open access to public spending data before the committee vote.',
      deadline: 'Open until Jul 18, 2026',
      status: { label: 'Open', severity: 'success' },
      responses: 12840,
      stance: null,
    },
    {
      id: 'con2',
      title: 'New downtown zoning plan',
      description: 'The city proposes rezoning the historic downtown for mixed residential and commercial use.',
      deadline: 'Open until Jul 25, 2026',
      status: { label: 'Open', severity: 'success' },
      responses: 6410,
      stance: null,
    },
  ]);
  readonly consultations = this._consultations.asReadonly();

  private readonly _surveys = signal<Survey[]>([
    {
      id: 'sur1',
      question: 'What should be the top budget priority next year?',
      context: 'Participatory budgeting · 8,204 responses',
      options: [
        { id: 'o1', label: 'Healthcare', votes: 3120 },
        { id: 'o2', label: 'Education', votes: 2680 },
        { id: 'o3', label: 'Public transit', votes: 1540 },
        { id: 'o4', label: 'Public safety', votes: 864 },
      ],
      votedOptionId: null,
    },
    {
      id: 'sur2',
      question: 'Should city council sessions be streamed live?',
      context: 'Governance poll · 5,110 responses',
      options: [
        { id: 'y', label: 'Yes, always', votes: 4380 },
        { id: 'n', label: 'Only major votes', votes: 610 },
        { id: 'x', label: 'No', votes: 120 },
      ],
      votedOptionId: null,
    },
  ]);
  readonly surveys = this._surveys.asReadonly();

  signPetition(id: string): void {
    this._petitions.update((list) =>
      list.map((p) =>
        p.id === id && !p.signed ? { ...p, signed: true, signatures: p.signatures + 1 } : p,
      ),
    );
  }

  setStance(id: string, stance: ConsultationStance): void {
    this._consultations.update((list) =>
      list.map((c) => {
        if (c.id !== id) {
          return c;
        }
        const responses = c.stance === null ? c.responses + 1 : c.responses;
        return { ...c, stance, responses };
      }),
    );
  }

  vote(surveyId: string, optionId: string): void {
    this._surveys.update((list) =>
      list.map((s) => {
        if (s.id !== surveyId || s.votedOptionId !== null) {
          return s;
        }
        return {
          ...s,
          votedOptionId: optionId,
          options: s.options.map((o) => (o.id === optionId ? { ...o, votes: o.votes + 1 } : o)),
        };
      }),
    );
  }
}
