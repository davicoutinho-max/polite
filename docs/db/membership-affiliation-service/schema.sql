-- ============================================================
-- Membership & Affiliation Service
-- The affiliation state-machine/saga, digital membership card, fee billing
-- Database: PostgreSQL 16
-- Why: A long-running saga needs durable, queryable state and strict status transitions — exactly what row-level ACID transactions are for.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE affiliation_status_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE affiliation_status_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO affiliation_status_options (code, label, sort_order) VALUES
  ('not_started', 'Not started', 1),
  ('requested', 'Requested', 2),
  ('under_review', 'Under review', 3),
  ('party_approved', 'Party approved', 4),
  ('electoral_justice', 'Sent to Electoral Justice', 5),
  ('affiliated', 'Affiliated', 6),
  ('rejected', 'Rejected', 7);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE fee_status_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE fee_status_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO fee_status_options (code, label, sort_order) VALUES
  ('pending', 'Pending', 1),
  ('paid', 'Paid', 2),
  ('overdue', 'Overdue', 3);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
-- Who/what triggered a status transition — an actor vocabulary, not a business status, but still fixed and worth parameterizing.
CREATE TABLE changed_by_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE changed_by_options IS 'Who/what triggered a status transition — an actor vocabulary, not a business status, but still fixed and worth parameterizing.';
INSERT INTO changed_by_options (code, label, sort_order) VALUES
  ('citizen', 'Citizen', 1),
  ('party', 'Party', 2),
  ('electoral_justice', 'Sent to Electoral Justice', 3),
  ('system', 'System', 4);

-- One saga instance per citizen-party pair. status only ever advances forward (see affiliation-lifecycle.bpmn).
CREATE TABLE affiliations (
  id                           uuid DEFAULT gen_random_uuid(),
  citizen_account_id           uuid NOT NULL,
  party_id                     uuid NOT NULL,
  status                       text NOT NULL DEFAULT 'not_started',
  requested_at                 timestamptz,
  updated_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (status) REFERENCES affiliation_status_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE affiliations IS 'One saga instance per citizen-party pair. status only ever advances forward (see affiliation-lifecycle.bpmn).';
CREATE UNIQUE INDEX uq_affiliation_active ON affiliations (citizen_account_id, party_id) WHERE status <> 'rejected';

-- Full audit trail of the saga — required given two external authorities (party, Electoral Justice) are involved.
CREATE TABLE affiliation_status_history (
  id                           uuid DEFAULT gen_random_uuid(),
  affiliation_id               uuid NOT NULL,
  from_status                  text,
  to_status                    text NOT NULL,
  changed_by                   text NOT NULL,
  changed_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (affiliation_id) REFERENCES affiliations (id) ON DELETE CASCADE,
  FOREIGN KEY (from_status) REFERENCES affiliation_status_options (code) ON DELETE RESTRICT,
  FOREIGN KEY (to_status) REFERENCES affiliation_status_options (code) ON DELETE RESTRICT,
  FOREIGN KEY (changed_by) REFERENCES changed_by_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE affiliation_status_history IS 'Full audit trail of the saga — required given two external authorities (party, Electoral Justice) are involved.';
CREATE INDEX idx_affiliation_history ON affiliation_status_history (affiliation_id);
COMMENT ON COLUMN affiliation_status_history.changed_by IS '''citizen'' | ''party'' | ''electoral_justice'' | ''system''';

CREATE TABLE membership_cards (
  affiliation_id               uuid,
  member_number                text NOT NULL UNIQUE,
  qr_payload                   text NOT NULL,
  issued_at                    timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (affiliation_id),
  FOREIGN KEY (affiliation_id) REFERENCES affiliations (id) ON DELETE CASCADE
);

CREATE TABLE membership_fees (
  id                           uuid DEFAULT gen_random_uuid(),
  affiliation_id               uuid NOT NULL,
  reference_period             text NOT NULL,
  amount_cents                 bigint NOT NULL,
  due_date                     date NOT NULL,
  status                       text NOT NULL DEFAULT 'pending',
  paid_at                      timestamptz,
  payment_intent_id            uuid,
  PRIMARY KEY (id),
  FOREIGN KEY (affiliation_id) REFERENCES affiliations (id) ON DELETE CASCADE,
  FOREIGN KEY (status) REFERENCES fee_status_options (code) ON DELETE RESTRICT
);
CREATE UNIQUE INDEX uq_fee_period ON membership_fees (affiliation_id, reference_period);
CREATE INDEX idx_fees_status_due ON membership_fees (status, due_date);
COMMENT ON COLUMN membership_fees.reference_period IS 'e.g. ''2026-07''';
COMMENT ON COLUMN membership_fees.payment_intent_id IS 'reference into payments-service.payment_intents.id — by value only';

-- ---- Domain events published ----
-- -> AffiliationRequested
-- -> AffiliationUnderReview
-- -> AffiliationConfirmed
-- -> MembershipCardIssued
-- -> MembershipFeeGenerated
-- -> MembershipFeeOverdue
-- ---- Domain events consumed ----
-- <- AffiliationRequestApproved
-- <- AffiliationRequestRejected
-- <- PaymentCaptured
