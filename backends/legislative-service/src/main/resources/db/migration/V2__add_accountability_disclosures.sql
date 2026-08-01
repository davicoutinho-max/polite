-- ============================================================
-- Accountability disclosures ("prestação de contas") — a politician declares a spent amount for
-- one of the real categories of public money they're accountable for, attaches supporting proof
-- (a receipt/invoice/statement), and an AI reviewer (assistant-service, backed by Gemini's
-- document understanding) checks whether the attached document actually supports the declared
-- amount. Every submission is kept (not just the latest) so a rejected attempt's AI feedback
-- stays visible while the politician retries — the section's current status is simply its most
-- recent submission's status.
-- ============================================================

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE accountability_category_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE accountability_category_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO accountability_category_options (code, label, sort_order) VALUES
  ('office_budget', 'Verba de Gabinete', 1),
  ('parliamentary_quota', 'Cota para o Exercício da Atividade Parlamentar (CEAP)', 2),
  ('parliamentary_amendments', 'Emendas Parlamentares', 3),
  ('travel_allowance', 'Diárias e Passagens', 4),
  ('advertising', 'Publicidade Institucional', 5);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE accountability_status_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE accountability_status_options IS 'Each submission is scored independently by the AI reviewer — there is no forward-only transition here, a politician may resubmit after a rejection.';
INSERT INTO accountability_status_options (code, label, sort_order) VALUES
  ('approved', 'Approved', 1),
  ('rejected', 'Rejected', 2);

CREATE TABLE accountability_disclosures (
  id                           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  politician_account_id        uuid NOT NULL,
  category                     text NOT NULL REFERENCES accountability_category_options (code) ON DELETE RESTRICT,
  declared_amount_cents        bigint NOT NULL CHECK (declared_amount_cents >= 0),
  document_url                 text NOT NULL,
  status                       text NOT NULL REFERENCES accountability_status_options (code) ON DELETE RESTRICT,
  extracted_amount_cents       bigint,
  ai_feedback                  text NOT NULL,
  submitted_at                 timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_accountability_disclosures_politician ON accountability_disclosures (politician_account_id, category, submitted_at DESC);
