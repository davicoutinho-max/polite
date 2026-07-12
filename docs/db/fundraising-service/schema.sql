-- ============================================================
-- Fundraising Service
-- Social-cause & party-initiative campaigns, contributions, public progress ledger
-- Database: PostgreSQL 16
-- Why: Moderate volume, strong consistency needs (goal/raised amounts must never drift from reality) — a clean relational fit that delegates all money movement to Payments.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE fundraiser_category_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE fundraiser_category_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO fundraiser_category_options (code, label, sort_order) VALUES
  ('social', 'Social cause', 1),
  ('party', 'Party', 2),
  ('humanitarian', 'Humanitarian aid', 3);

CREATE TABLE fundraisers (
  id                           uuid DEFAULT gen_random_uuid(),
  organizer_account_id         uuid NOT NULL,
  title                        text NOT NULL,
  description                  text,
  category                     text NOT NULL,
  goal_cents                   bigint NOT NULL,
  raised_cents                 bigint NOT NULL DEFAULT 0,
  supporters_count             integer NOT NULL DEFAULT 0,
  deadline                     date,
  ledger_public                boolean NOT NULL DEFAULT true,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (category) REFERENCES fundraiser_category_options (code) ON DELETE RESTRICT
);
CREATE INDEX idx_fundraisers_category ON fundraisers (category);

CREATE TABLE contributions (
  id                           uuid DEFAULT gen_random_uuid(),
  fundraiser_id                uuid NOT NULL,
  supporter_account_id         uuid NOT NULL,
  amount_cents                 bigint NOT NULL CHECK (amount_cents > 0),
  payment_intent_id            uuid NOT NULL UNIQUE,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (fundraiser_id) REFERENCES fundraisers (id) ON DELETE RESTRICT
);
CREATE INDEX idx_contributions_fundraiser ON contributions (fundraiser_id, created_at);
CREATE INDEX idx_contributions_supporter ON contributions (supporter_account_id);
COMMENT ON COLUMN contributions.payment_intent_id IS 'reference into payments-service.payment_intents.id';

-- ---- Domain events published ----
-- -> FundraiserCreated
-- -> ContributionReceived
-- -> FundraiserGoalReached
-- ---- Domain events consumed ----
-- <- PaymentCaptured
