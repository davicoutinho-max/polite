-- ============================================================
-- Notification Service
-- Event-driven per-user alert inbox — pure consumer of every other service's domain events
-- Database: PostgreSQL 16, time-partitioned + aggressive TTL purge, behind a message broker (Kafka/RabbitMQ)
-- Why: This service can be the single highest write-fanout point in the system (one popular post → thousands of notification rows) — partitioning and short retention matter more than query flexibility.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE notification_category_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE notification_category_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO notification_category_options (code, label, sort_order) VALUES
  ('project', 'New bill/project', 1),
  ('pec', 'New PEC', 2),
  ('party', 'Party', 3),
  ('vote', 'Vote registered', 4),
  ('cpi', 'New inquiry (CPI)', 5),
  ('campaign', 'Fundraising campaign', 6);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE platform_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE platform_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO platform_options (code, label, sort_order) VALUES
  ('ios', 'iOS', 1),
  ('android', 'Android', 2),
  ('web', 'Web', 3);

-- engine: PostgreSQL, PARTITION BY RANGE (created_at) — weekly partitions, auto-dropped after 90 days
-- created_at joins id in the primary key because Postgres requires the partition column in every unique constraint on a partitioned table.
CREATE TABLE notifications (
  id                           uuid DEFAULT gen_random_uuid(),
  recipient_account_id         uuid NOT NULL,
  category                     text NOT NULL,
  icon                         text,
  title                        text NOT NULL,
  message                      text NOT NULL,
  link                         text,
  source_event_id              text NOT NULL,
  read                         boolean NOT NULL DEFAULT false,
  created_at                   timestamptz DEFAULT now(),
  PRIMARY KEY (id, created_at),
  FOREIGN KEY (category) REFERENCES notification_category_options (code) ON DELETE RESTRICT
) PARTITION BY RANGE (created_at);
COMMENT ON TABLE notifications IS 'created_at joins id in the primary key because Postgres requires the partition column in every unique constraint on a partitioned table.';
CREATE INDEX idx_notifications_recipient ON notifications (recipient_account_id, created_at DESC);
CREATE UNIQUE INDEX uq_notifications_source ON notifications (recipient_account_id, source_event_id, created_at);
COMMENT ON COLUMN notifications.source_event_id IS 'the upstream event id; deduped together with a short-lived Redis SETNX at ingest time since the partitioned unique index alone cannot span partitions perfectly';
-- Bootstrap partition only — a scheduled job (pg_partman or equivalent) must
-- create real time-bucket partitions ahead of need in production; without it
-- every row simply lands here, so the table is correct, just not yet fast.
CREATE TABLE notifications_default PARTITION OF notifications DEFAULT;

CREATE TABLE push_tokens (
  account_id                   uuid,
  platform                     text,
  token                        text,
  updated_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, platform, token),
  FOREIGN KEY (platform) REFERENCES platform_options (code) ON DELETE RESTRICT
);

-- ---- Domain events published ----
-- ---- Domain events consumed ----
-- <- AffiliationRequested / AffiliationConfirmed (Membership & Affiliation)
-- <- MembershipFeeGenerated / MembershipFeeOverdue (Membership & Affiliation)
-- <- PostPublished (Feed & Content, fanned out to followers)
-- <- FundraiserCreated / FundraiserGoalReached (Fundraising)
-- <- PartyProfileUpdated (Party Management)
-- <- ...every other "note-worthy" event in the system
