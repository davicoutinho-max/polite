-- ============================================================
-- Party Management Service
-- Affiliation requests, members, representatives, party self-service content
-- Database: PostgreSQL 16
-- Why: Transactional workflows (approve/reject, link/unlink) with moderate write volume — a straightforward relational fit.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE party_office_scope_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE party_office_scope_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO party_office_scope_options (code, label, sort_order) VALUES
  ('nacional', 'Nacional', 1),
  ('estadual', 'Estadual', 2),
  ('municipal', 'Municipal', 3);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE affiliation_request_status_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE affiliation_request_status_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO affiliation_request_status_options (code, label, sort_order) VALUES
  ('pending', 'Pending', 1),
  ('approved', 'Approved', 2),
  ('rejected', 'Rejected', 3);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE party_member_status_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE party_member_status_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO party_member_status_options (code, label, sort_order) VALUES
  ('active', 'Active', 1),
  ('suspended', 'Suspended', 2);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
-- Shared UI severity vocabulary for event tags — duplicated locally in Feed & Content, which uses the same set for post tags.
CREATE TABLE tag_severity_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE tag_severity_options IS 'Shared UI severity vocabulary for event tags — duplicated locally in Feed & Content, which uses the same set for post tags.';
INSERT INTO tag_severity_options (code, label, sort_order) VALUES
  ('success', 'Success', 1),
  ('warning', 'Warning', 2),
  ('danger', 'Danger', 3),
  ('info', 'Info', 4),
  ('neutral', 'Neutral', 5),
  ('secondary', 'Secondary', 6),
  ('primary', 'Primary', 7);

-- The party's own editable "about" content — separate from the legal registry row in Platform Configuration.
CREATE TABLE party_profiles (
  party_id                     uuid,
  history                      text,
  program                      text,
  statute_url                  text,
  cover_url                    text,
  updated_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (party_id)
);
COMMENT ON TABLE party_profiles IS 'The party''s own editable "about" content — separate from the legal registry row in Platform Configuration.';

-- Regional directories (Nacional/Estadual/Municipal), each with its own leader.
CREATE TABLE party_offices (
  id                           uuid DEFAULT gen_random_uuid(),
  party_id                     uuid NOT NULL,
  scope                        text NOT NULL,
  location                     text NOT NULL,
  leader_name                  text,
  member_count                 integer NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  FOREIGN KEY (scope) REFERENCES party_office_scope_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE party_offices IS 'Regional directories (Nacional/Estadual/Municipal), each with its own leader.';
CREATE INDEX idx_party_offices_party ON party_offices (party_id);

CREATE TABLE party_events (
  id                           uuid DEFAULT gen_random_uuid(),
  party_id                     uuid NOT NULL,
  title                        text NOT NULL,
  event_date                   date NOT NULL,
  location                     text,
  tag_label                    text,
  tag_severity                 text,
  PRIMARY KEY (id),
  FOREIGN KEY (tag_severity) REFERENCES tag_severity_options (code) ON DELETE RESTRICT
);
CREATE INDEX idx_party_events_party ON party_events (party_id, event_date);

-- Politician ↔ party linkage, either from registration (flow 02) or Platform Admin reassignment (flow 03).
CREATE TABLE party_representatives (
  id                           uuid DEFAULT gen_random_uuid(),
  party_id                     uuid NOT NULL,
  politician_account_id        uuid NOT NULL,
  role_title                   text,
  linked_at                    timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id)
);
COMMENT ON TABLE party_representatives IS 'Politician ↔ party linkage, either from registration (flow 02) or Platform Admin reassignment (flow 03).';
CREATE UNIQUE INDEX uq_party_rep ON party_representatives (party_id, politician_account_id);
CREATE INDEX idx_party_reps_politician ON party_representatives (politician_account_id);

-- Party-side review step of the Affiliation Lifecycle (flow 04) — approve/reject only; the multi-stage saga itself is owned by Membership & Affiliation.
CREATE TABLE affiliation_requests (
  id                           uuid DEFAULT gen_random_uuid(),
  party_id                     uuid NOT NULL,
  citizen_account_id           uuid NOT NULL,
  city                         text,
  status                       text NOT NULL DEFAULT 'pending',
  requested_at                 timestamptz NOT NULL DEFAULT now(),
  decided_at                   timestamptz,
  PRIMARY KEY (id),
  FOREIGN KEY (status) REFERENCES affiliation_request_status_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE affiliation_requests IS 'Party-side review step of the Affiliation Lifecycle (flow 04) — approve/reject only; the multi-stage saga itself is owned by Membership & Affiliation.';
CREATE INDEX idx_affiliation_requests_party_status ON affiliation_requests (party_id, status);

CREATE TABLE party_members (
  id                           uuid DEFAULT gen_random_uuid(),
  party_id                     uuid NOT NULL,
  citizen_account_id           uuid NOT NULL,
  city                         text,
  status                       text NOT NULL DEFAULT 'active',
  joined_at                    timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (status) REFERENCES party_member_status_options (code) ON DELETE RESTRICT
);
CREATE UNIQUE INDEX uq_party_member ON party_members (party_id, citizen_account_id);

-- ---- Domain events published ----
-- -> PartyProfileUpdated
-- -> PoliticianRegistered(politician_account_id, party_id, cpf_hash, cnpj_hash)
-- -> RepresentativeLinked
-- -> RepresentativeRemoved
-- -> AffiliationRequestApproved
-- -> AffiliationRequestRejected
-- -> PartyMemberStatusChanged
-- ---- Domain events consumed ----
-- <- PartyRegistered
-- <- AffiliationRequested
