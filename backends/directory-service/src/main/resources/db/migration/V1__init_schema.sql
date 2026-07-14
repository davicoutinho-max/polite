-- ============================================================
-- Directory Service
-- Public politician & party catalog — the searchable read model
-- Database: PostgreSQL 16 + Elasticsearch (search index)
-- Why: Read-dominated and filter-heavy (by state, level, spectrum, name). Postgres stays the system of record; Elasticsearch is a derived, rebuildable index for full-text/faceted search at scale.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE gov_level_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE gov_level_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO gov_level_options (code, label, sort_order) VALUES
  ('federal', 'Federal', 1),
  ('state', 'State', 2),
  ('municipal', 'Municipal', 3);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE party_spectrum_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE party_spectrum_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO party_spectrum_options (code, label, sort_order) VALUES
  ('left', 'Left', 1),
  ('center_left', 'Center-left', 2),
  ('center', 'Center', 3),
  ('center_right', 'Center-right', 4),
  ('right', 'Right', 5);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE follow_target_type_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE follow_target_type_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO follow_target_type_options (code, label, sort_order) VALUES
  ('politician', 'Politician', 1),
  ('party', 'Party', 2);

-- Eventually-consistent projection, built from Identity + Party Management events. Never written to directly by users.
CREATE TABLE politicians (
  account_id                   uuid,
  name                         text NOT NULL,
  handle                       citext NOT NULL UNIQUE,
  avatar_url                   text,
  verified                     boolean NOT NULL DEFAULT false,
  office                       text,
  level                        text,
  party_id                     uuid,
  party_acronym                text,
  state                        text,
  followers_count              integer NOT NULL DEFAULT 0,
  bills_count                  integer NOT NULL DEFAULT 0,
  updated_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id),
  FOREIGN KEY (level) REFERENCES gov_level_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE politicians IS 'Eventually-consistent projection, built from Identity + Party Management events. Never written to directly by users.';
CREATE INDEX idx_politicians_party ON politicians (party_id);
CREATE INDEX idx_politicians_state ON politicians (state);
CREATE INDEX idx_politicians_level ON politicians (level);
COMMENT ON COLUMN politicians.account_id IS 'value-equal to identity-service.accounts.id, no cross-service FK';

-- Public party catalog — the platform-wide legal registry lives in Platform Configuration; this is its read-facing shadow.
CREATE TABLE parties (
  id                           uuid,
  name                         text NOT NULL,
  acronym                      text NOT NULL UNIQUE,
  number                       integer NOT NULL,
  ideology                     text,
  spectrum                     text,
  founded_year                 integer,
  president                    text,
  logo_url                     text,
  member_count                 integer NOT NULL DEFAULT 0,
  updated_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (spectrum) REFERENCES party_spectrum_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE parties IS 'Public party catalog — the platform-wide legal registry lives in Platform Configuration; this is its read-facing shadow.';
CREATE INDEX idx_parties_spectrum ON parties (spectrum);

-- Social graph edge. Powers followers_count and the Feed & Content "Following" sort (read via API, not replicated).
CREATE TABLE follows (
  follower_account_id          uuid,
  target_type                  text,
  target_id                    uuid,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (follower_account_id, target_type, target_id),
  FOREIGN KEY (target_type) REFERENCES follow_target_type_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE follows IS 'Social graph edge. Powers followers_count and the Feed & Content "Following" sort (read via API, not replicated).';
CREATE INDEX idx_follows_target ON follows (target_type, target_id);

-- ---- Domain events published ----
-- -> FollowCreated(follower_account_id, target_type, target_id)
-- -> FollowRemoved(follower_account_id, target_type, target_id)
-- ---- Domain events consumed ----
-- <- AccountRegistered
-- <- PoliticianRegistered
-- <- PoliticianReassigned
-- <- PartyRegistered
-- <- RepresentativeLinked
