-- ============================================================
-- Legislative Service
-- Politician dossier extensions, legislative items (bills/PECs/CPIs), committees, votes,
-- attendance, transparency reports, career milestones
-- Database: PostgreSQL 16
-- Why: directory-service intentionally stays a lean, read-heavy public directory (name, handle,
-- office, party, followers). Everything below is a deep, low-traffic extension of a politician's
-- profile that directory-service was never meant to own — this service owns it instead, keyed
-- everywhere by politician_account_id (directory-service's identity), never duplicating
-- directory-service's own fields.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE legislative_item_category_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE legislative_item_category_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO legislative_item_category_options (code, label, sort_order) VALUES
  ('project', 'Bill / Project', 1),
  ('pec', 'Constitutional Amendment (PEC)', 2),
  ('cpi', 'Inquiry (CPI)', 3);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
-- Forward-only pending->in_committee->floor_vote->passed, except the one-way exit to
-- 'rejected' from any status before passed — mirrors payments-service's PaymentStatus /
-- membership-affiliation-service's AffiliationStatus escape-valve pattern.
CREATE TABLE legislative_item_status_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE legislative_item_status_options IS 'Forward-only filed->in_committee->floor_vote->passed, except the one-way exit to rejected from any earlier status.';
INSERT INTO legislative_item_status_options (code, label, sort_order) VALUES
  ('filed', 'Filed', 1),
  ('in_committee', 'In Committee', 2),
  ('floor_vote', 'Floor Vote', 3),
  ('passed', 'Passed', 4),
  ('rejected', 'Rejected', 5);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE committee_kind_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE committee_kind_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO committee_kind_options (code, label, sort_order) VALUES
  ('committee', 'Committee', 1),
  ('front', 'Parliamentary Front', 2),
  ('cpi', 'Inquiry Committee (CPI)', 3);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE vote_choice_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE vote_choice_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO vote_choice_options (code, label, sort_order) VALUES
  ('yes', 'Yes', 1),
  ('no', 'No', 2),
  ('abstain', 'Abstain', 3),
  ('absent', 'Absent', 4);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE social_platform_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE social_platform_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO social_platform_options (code, label, sort_order) VALUES
  ('website', 'Website', 1),
  ('instagram', 'Instagram', 2),
  ('x', 'X (Twitter)', 3),
  ('facebook', 'Facebook', 4),
  ('youtube', 'YouTube', 5),
  ('linkedin', 'LinkedIn', 6),
  ('tiktok', 'TikTok', 7);

-- One row per politician account, created lazily on the first PoliticianRegistered/
-- RepresentativeLinked event this service consumes for that account (see "Domain events
-- consumed" below) — directory-service remains the source of truth for whether the account
-- exists at all; this table only ever extends it.
CREATE TABLE politician_dossier_extensions (
  politician_account_id        uuid,
  education                    text,
  profession                   text,
  patrimony                    text,
  email                        citext,
  phone                        text,
  office_detail                text,
  speeches_count               integer NOT NULL DEFAULT 0,
  interviews_count             integer NOT NULL DEFAULT 0,
  trips_count                  integer NOT NULL DEFAULT 0,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (politician_account_id)
);
COMMENT ON TABLE politician_dossier_extensions IS 'Created lazily from PoliticianRegistered/RepresentativeLinked — directory-service stays the source of truth for whether the account itself exists.';
COMMENT ON COLUMN politician_dossier_extensions.speeches_count IS 'A bare counter for this pass, not backed by individual speech records — see the service-level scope note.';

CREATE TABLE mandates (
  id                           uuid DEFAULT gen_random_uuid(),
  politician_account_id        uuid NOT NULL,
  role                         text NOT NULL,
  period                       text NOT NULL,
  current                      boolean NOT NULL DEFAULT false,
  PRIMARY KEY (id),
  FOREIGN KEY (politician_account_id) REFERENCES politician_dossier_extensions (politician_account_id) ON DELETE CASCADE
);
CREATE INDEX idx_mandates_politician ON mandates (politician_account_id);

CREATE TABLE social_links (
  id                           uuid DEFAULT gen_random_uuid(),
  politician_account_id        uuid NOT NULL,
  platform                     text NOT NULL,
  label                        text NOT NULL,
  handle                       text NOT NULL,
  url                          text NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (politician_account_id) REFERENCES politician_dossier_extensions (politician_account_id) ON DELETE CASCADE,
  FOREIGN KEY (platform) REFERENCES social_platform_options (code) ON DELETE RESTRICT
);
CREATE INDEX idx_social_links_politician ON social_links (politician_account_id);

CREATE TABLE team_members (
  id                           uuid DEFAULT gen_random_uuid(),
  politician_account_id        uuid NOT NULL,
  name                         text NOT NULL,
  role                         text NOT NULL,
  avatar_url                   text,
  PRIMARY KEY (id),
  FOREIGN KEY (politician_account_id) REFERENCES politician_dossier_extensions (politician_account_id) ON DELETE CASCADE
);
CREATE INDEX idx_team_members_politician ON team_members (politician_account_id);

-- Unifies what the frontend used to mock as 5 separate arrays (projects/approvedLaws/
-- rejectedLaws/pecs/cpis) into one aggregate: category is the instrument type (project/pec/cpi),
-- status is its current workflow stage — "approved law" is simply category=project AND
-- status=passed, not a separate shape.
CREATE TABLE legislative_items (
  id                           uuid DEFAULT gen_random_uuid(),
  politician_account_id        uuid NOT NULL,
  reference                    text NOT NULL,
  title                        text NOT NULL,
  summary                      text,
  category                     text NOT NULL,
  status                       text NOT NULL DEFAULT 'filed',
  item_date                    date NOT NULL,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (politician_account_id) REFERENCES politician_dossier_extensions (politician_account_id) ON DELETE CASCADE,
  FOREIGN KEY (category) REFERENCES legislative_item_category_options (code) ON DELETE RESTRICT,
  FOREIGN KEY (status) REFERENCES legislative_item_status_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE legislative_items IS 'Unifies the frontend mock''s 5 separate arrays (projects/approvedLaws/rejectedLaws/pecs/cpis) into one aggregate: category is the instrument type, status is its workflow stage — "approved law" is category=project AND status=passed, not a separate shape.';
CREATE INDEX idx_legislative_items_politician ON legislative_items (politician_account_id, item_date DESC);
CREATE INDEX idx_legislative_items_recent ON legislative_items (created_at DESC);

CREATE TABLE legislative_item_cosponsors (
  legislative_item_id          uuid,
  politician_account_id        uuid,
  PRIMARY KEY (legislative_item_id, politician_account_id),
  FOREIGN KEY (legislative_item_id) REFERENCES legislative_items (id) ON DELETE CASCADE
);

CREATE TABLE committee_memberships (
  id                           uuid DEFAULT gen_random_uuid(),
  politician_account_id        uuid NOT NULL,
  name                         text NOT NULL,
  role                         text NOT NULL,
  kind                         text NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (politician_account_id) REFERENCES politician_dossier_extensions (politician_account_id) ON DELETE CASCADE,
  FOREIGN KEY (kind) REFERENCES committee_kind_options (code) ON DELETE RESTRICT
);
CREATE INDEX idx_committee_memberships_politician ON committee_memberships (politician_account_id);

-- legislative_item_id is nullable — not every recorded vote corresponds to a formally tracked
-- legislative item in this system (e.g. procedural votes); matter is always a free-text label
-- either way, exactly like the original mock's VoteRecord.matter.
CREATE TABLE vote_records (
  id                           uuid DEFAULT gen_random_uuid(),
  politician_account_id        uuid NOT NULL,
  legislative_item_id          uuid,
  matter                       text NOT NULL,
  vote_date                    date NOT NULL,
  choice                       text NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (politician_account_id) REFERENCES politician_dossier_extensions (politician_account_id) ON DELETE CASCADE,
  FOREIGN KEY (legislative_item_id) REFERENCES legislative_items (id) ON DELETE SET NULL,
  FOREIGN KEY (choice) REFERENCES vote_choice_options (code) ON DELETE RESTRICT
);
CREATE INDEX idx_vote_records_politician ON vote_records (politician_account_id, vote_date DESC);

-- presence_rate is intentionally NOT a column — always computed from present/absent at read
-- time (the frontend mock stored a rate that didn't even match its own present/absent numbers;
-- this fixes that class of bug structurally by not storing a derivable value at all).
CREATE TABLE attendance_records (
  politician_account_id        uuid,
  present                      integer NOT NULL DEFAULT 0,
  absent                       integer NOT NULL DEFAULT 0,
  updated_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (politician_account_id),
  FOREIGN KEY (politician_account_id) REFERENCES politician_dossier_extensions (politician_account_id) ON DELETE CASCADE
);
COMMENT ON TABLE attendance_records IS 'presence_rate is deliberately not a column — always computed from present/absent at read time.';

-- total_expense_cents is the sum of expense_lines, always recomputed at write time, never
-- trusted as an independent input — same reasoning as attendance's computed presence_rate.
CREATE TABLE transparency_reports (
  politician_account_id        uuid,
  total_expense_cents          bigint NOT NULL DEFAULT 0,
  last_update                  date NOT NULL DEFAULT CURRENT_DATE,
  PRIMARY KEY (politician_account_id),
  FOREIGN KEY (politician_account_id) REFERENCES politician_dossier_extensions (politician_account_id) ON DELETE CASCADE
);

CREATE TABLE transparency_metrics (
  id                           uuid DEFAULT gen_random_uuid(),
  politician_account_id        uuid NOT NULL,
  icon                         text,
  label                        text NOT NULL,
  value_cents                  bigint NOT NULL,
  caption                      text,
  period                       text NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (politician_account_id) REFERENCES politician_dossier_extensions (politician_account_id) ON DELETE CASCADE
);
COMMENT ON COLUMN transparency_metrics.value_cents IS 'Real integer cents, not a preformatted display string like the original frontend mock (e.g. "R$ 41.650") — formatting happens client-side.';
CREATE INDEX idx_transparency_metrics_politician ON transparency_metrics (politician_account_id);

-- share (0-100 % of totalExpense) is NOT a column — always computed from
-- amount_cents / transparency_reports.total_expense_cents at read time, fixing the original
-- mock's hand-set, internally-inconsistent shares.
CREATE TABLE expense_lines (
  id                           uuid DEFAULT gen_random_uuid(),
  politician_account_id        uuid NOT NULL,
  category                     text NOT NULL,
  amount_cents                 bigint NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (politician_account_id) REFERENCES politician_dossier_extensions (politician_account_id) ON DELETE CASCADE
);
COMMENT ON TABLE expense_lines IS 'share (% of total) is deliberately not a column — always computed from amount_cents / transparency_reports.total_expense_cents at read time.';
CREATE INDEX idx_expense_lines_politician ON expense_lines (politician_account_id);

CREATE TABLE career_milestones (
  id                           uuid DEFAULT gen_random_uuid(),
  politician_account_id        uuid NOT NULL,
  year                         smallint NOT NULL,
  title                        text NOT NULL,
  detail                       text,
  PRIMARY KEY (id),
  FOREIGN KEY (politician_account_id) REFERENCES politician_dossier_extensions (politician_account_id) ON DELETE CASCADE
);
CREATE INDEX idx_career_milestones_politician ON career_milestones (politician_account_id, year);

-- ---- Domain events published ----
-- -> LegislativeItemFiled(legislative_item_id, politician_account_id, category, reference) — consumed by directory-service to drive its bills_count projection column, and by activity-feed-service.
-- -> LegislativeItemStatusChanged(legislative_item_id, politician_account_id, status) — consumed by activity-feed-service.
-- -> VoteCast(vote_record_id, politician_account_id, matter, choice) — consumed by activity-feed-service.
-- -> CommitteeMembershipChanged(committee_membership_id, politician_account_id, name) — consumed by activity-feed-service.
-- ---- Domain events consumed ----
-- <- PoliticianRegistered (Party Management) — lazily creates a politician_dossier_extensions stub row.
-- <- RepresentativeLinked (Party Management) — lazily creates a politician_dossier_extensions stub row if one doesn't exist yet.
