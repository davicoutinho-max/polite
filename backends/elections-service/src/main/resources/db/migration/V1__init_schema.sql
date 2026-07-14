-- ============================================================
-- Elections Service
-- Public election calendar and candidacy lookup — read-heavy, visitor-accessible
-- Database: PostgreSQL 16, cached at the edge
-- Why: Almost pure read traffic from an unauthenticated audience; a small relational table set behind a CDN cache is the simplest thing that works.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE election_scope_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE election_scope_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO election_scope_options (code, label, sort_order) VALUES
  ('nacional', 'Nacional', 1),
  ('estadual', 'Estadual', 2),
  ('municipal', 'Municipal', 3);

CREATE TABLE elections (
  id                           uuid DEFAULT gen_random_uuid(),
  title                        text NOT NULL,
  scope                        text NOT NULL,
  election_date                date NOT NULL,
  description                  text,
  PRIMARY KEY (id),
  FOREIGN KEY (scope) REFERENCES election_scope_options (code) ON DELETE RESTRICT
);
CREATE INDEX idx_elections_date ON elections (election_date);

CREATE TABLE election_candidacies (
  election_id                  uuid,
  politician_account_id        uuid,
  PRIMARY KEY (election_id, politician_account_id),
  FOREIGN KEY (election_id) REFERENCES elections (id)
);
COMMENT ON COLUMN election_candidacies.politician_account_id IS 'resolved against Directory Service at query time, not replicated here';

-- ---- Domain events published ----
-- ---- Domain events consumed ----
