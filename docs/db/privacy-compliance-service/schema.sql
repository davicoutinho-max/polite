-- ============================================================
-- Privacy & Compliance Service
-- LGPD consent, data export and account erasure — orchestrates every other service, owns none of their data
-- Database: PostgreSQL 16
-- Why: Low volume, high audit/legal importance — a durable, queryable trail of every consent change and erasure request is the entire point.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE export_status_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE export_status_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO export_status_options (code, label, sort_order) VALUES
  ('pending', 'Pending', 1),
  ('processing', 'Processing', 2),
  ('ready', 'Ready', 3),
  ('failed', 'Failed', 4);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE deletion_status_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE deletion_status_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO deletion_status_options (code, label, sort_order) VALUES
  ('pending', 'Pending', 1),
  ('confirmed', 'Confirmed', 2),
  ('processing', 'Processing', 3),
  ('completed', 'Completed', 4),
  ('canceled', 'Canceled', 5);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE consent_purpose_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE consent_purpose_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO consent_purpose_options (code, label, sort_order) VALUES
  ('essential', 'Essential', 1),
  ('analytics', 'Analytics', 2),
  ('personalization', 'Personalization', 3),
  ('marketing', 'Marketing', 4);

CREATE TABLE consent_records (
  account_id                   uuid,
  purpose                      text,
  granted                      boolean NOT NULL,
  updated_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, purpose),
  FOREIGN KEY (purpose) REFERENCES consent_purpose_options (code) ON DELETE RESTRICT
);

CREATE TABLE data_export_requests (
  id                           uuid DEFAULT gen_random_uuid(),
  account_id                   uuid NOT NULL,
  status                       text NOT NULL DEFAULT 'pending',
  requested_at                 timestamptz NOT NULL DEFAULT now(),
  completed_at                 timestamptz,
  download_url                 text,
  expires_at                   timestamptz,
  PRIMARY KEY (id),
  FOREIGN KEY (status) REFERENCES export_status_options (code) ON DELETE RESTRICT
);
CREATE INDEX idx_export_account ON data_export_requests (account_id);

CREATE TABLE account_deletion_requests (
  id                           uuid DEFAULT gen_random_uuid(),
  account_id                   uuid NOT NULL,
  status                       text NOT NULL DEFAULT 'pending',
  requested_at                 timestamptz NOT NULL DEFAULT now(),
  completed_at                 timestamptz,
  PRIMARY KEY (id),
  FOREIGN KEY (status) REFERENCES deletion_status_options (code) ON DELETE RESTRICT
);
CREATE INDEX idx_deletion_account ON account_deletion_requests (account_id);

-- Every other microservice reports back here once it has erased/anonymized its own copy of the account's data — the saga is "complete" only when every expected service has a row.
CREATE TABLE erasure_audit_log (
  id                           bigint GENERATED ALWAYS AS IDENTITY,
  deletion_request_id          uuid NOT NULL,
  service_name                 text NOT NULL,
  erased_at                    timestamptz NOT NULL DEFAULT now(),
  record_count                 integer,
  PRIMARY KEY (id),
  FOREIGN KEY (deletion_request_id) REFERENCES account_deletion_requests (id) ON DELETE CASCADE
);
COMMENT ON TABLE erasure_audit_log IS 'Every other microservice reports back here once it has erased/anonymized its own copy of the account''s data — the saga is "complete" only when every expected service has a row.';
CREATE UNIQUE INDEX uq_erasure_service ON erasure_audit_log (deletion_request_id, service_name);

-- ---- Domain events published ----
-- -> ConsentUpdated
-- -> DataExportRequested
-- -> AccountDeletionRequested
-- ---- Domain events consumed ----
-- <- <ServiceName>ErasureCompleted — one per microservice, e.g. FeedContentErasureCompleted, MessagingErasureCompleted
