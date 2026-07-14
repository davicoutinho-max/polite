-- ============================================================
-- Payments & Billing Service
-- Centralized payment-gateway integration, ledger and idempotency for every money movement on the platform
-- Database: PostgreSQL 16
-- Why: Money demands strict ACID, immutable audit trails and exactly-once semantics — the least polyglot-friendly domain in the system by design.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE payment_purpose_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE payment_purpose_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO payment_purpose_options (code, label, sort_order) VALUES
  ('membership_fee', 'Membership fee', 1),
  ('fundraising_contribution', 'Fundraising contribution', 2);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE payment_status_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE payment_status_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO payment_status_options (code, label, sort_order) VALUES
  ('created', 'Created', 1),
  ('authorized', 'Authorized', 2),
  ('captured', 'Captured', 3),
  ('failed', 'Failed', 4),
  ('refunded', 'Refunded', 5),
  ('canceled', 'Canceled', 6);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE ledger_direction_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE ledger_direction_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO ledger_direction_options (code, label, sort_order) VALUES
  ('debit', 'Debit', 1),
  ('credit', 'Credit', 2);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE payment_gateway_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE payment_gateway_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO payment_gateway_options (code, label, sort_order) VALUES
  ('pix', 'Pix', 1),
  ('card', 'Card', 2),
  ('boleto', 'Boleto', 3);

-- One row per attempted charge, shared by Membership fee payment and Fundraising contribution flows.
CREATE TABLE payment_intents (
  id                           uuid DEFAULT gen_random_uuid(),
  purpose                      text NOT NULL,
  reference_id                 uuid NOT NULL,
  payer_account_id             uuid NOT NULL,
  payee_id                     uuid NOT NULL,
  amount_cents                 bigint NOT NULL CHECK (amount_cents > 0),
  currency                     char(3) NOT NULL DEFAULT 'BRL',
  status                       text NOT NULL DEFAULT 'created',
  gateway                      text NOT NULL,
  gateway_ref                  text,
  idempotency_key              text NOT NULL UNIQUE,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  updated_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (purpose) REFERENCES payment_purpose_options (code) ON DELETE RESTRICT,
  FOREIGN KEY (status) REFERENCES payment_status_options (code) ON DELETE RESTRICT,
  FOREIGN KEY (gateway) REFERENCES payment_gateway_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE payment_intents IS 'One row per attempted charge, shared by Membership fee payment and Fundraising contribution flows.';
CREATE INDEX idx_intents_payer ON payment_intents (payer_account_id);
CREATE INDEX idx_intents_reference ON payment_intents (purpose, reference_id);
COMMENT ON COLUMN payment_intents.reference_id IS 'opaque id of the fee or contribution being paid — this service does not know their internal shape';
COMMENT ON COLUMN payment_intents.payee_id IS 'party_id or fundraiser_id depending on purpose';

-- Append-only (INSERT-only grants, no UPDATE/DELETE) — this is the source of truth behind every "public ledger" bar shown in Fundraising.
CREATE TABLE ledger_entries (
  id                           bigint GENERATED ALWAYS AS IDENTITY,
  payment_intent_id            uuid NOT NULL,
  account_id                   uuid NOT NULL,
  direction                    text NOT NULL,
  amount_cents                 bigint NOT NULL,
  running_balance_cents        bigint NOT NULL,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (payment_intent_id) REFERENCES payment_intents (id) ON DELETE RESTRICT,
  FOREIGN KEY (direction) REFERENCES ledger_direction_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE ledger_entries IS 'Append-only (INSERT-only grants, no UPDATE/DELETE) — this is the source of truth behind every "public ledger" bar shown in Fundraising.';
CREATE INDEX idx_ledger_account ON ledger_entries (account_id, created_at);

-- Only a vault token is stored — raw card data never touches this database (PCI scope stays with the gateway).
CREATE TABLE payment_methods (
  id                           uuid DEFAULT gen_random_uuid(),
  account_id                   uuid NOT NULL,
  type                         text NOT NULL,
  token_ref                    text NOT NULL,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (type) REFERENCES payment_gateway_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE payment_methods IS 'Only a vault token is stored — raw card data never touches this database (PCI scope stays with the gateway).';

-- Transactional outbox — written in the same DB transaction as the payment_intents row, published by a relay process. Guarantees no "charged but event lost" gap.
CREATE TABLE outbox_events (
  id                           uuid DEFAULT gen_random_uuid(),
  aggregate_type               text NOT NULL DEFAULT 'payment_intent',
  aggregate_id                 uuid NOT NULL,
  event_type                   text NOT NULL,
  payload                      jsonb NOT NULL,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  published_at                 timestamptz,
  PRIMARY KEY (id)
);
COMMENT ON TABLE outbox_events IS 'Transactional outbox — written in the same DB transaction as the payment_intents row, published by a relay process. Guarantees no "charged but event lost" gap.';
CREATE INDEX idx_outbox_unpublished ON outbox_events (created_at) WHERE published_at IS NULL;

-- ---- Domain events published ----
-- -> PaymentAuthorized
-- -> PaymentCaptured
-- -> PaymentFailed
-- -> PaymentRefunded
-- ---- Domain events consumed ----
