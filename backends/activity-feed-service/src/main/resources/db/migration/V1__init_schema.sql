-- ============================================================
-- Activity Feed Service
-- Cross-service activity timeline — pure consumer read-model, same architectural family as
-- Notification Service (see its schema.sql), but for a profile's own activity feed rather than
-- a personal alert inbox. Publishes nothing.
-- Database: PostgreSQL 16
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
-- Only types with a real upstream producer are listed — the frontend mock's 'honor'/'event'/
-- 'accounts' types have no real event anywhere in the platform and are intentionally dropped.
CREATE TABLE timeline_event_type_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE timeline_event_type_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO timeline_event_type_options (code, label, sort_order) VALUES
  ('vote', 'Vote registered', 1),
  ('project', 'New bill/project filed', 2),
  ('pec', 'New PEC filed', 3),
  ('cpi', 'New inquiry (CPI) filed', 4),
  ('status_change', 'Legislative item status changed', 5),
  ('committee', 'Committee membership', 6),
  ('video', 'Video published', 7),
  ('post', 'Post published', 8),
  ('party_change', 'Party affiliation changed', 9),
  ('campaign', 'Fundraising goal reached', 10);

-- actor_name_denormalized is resolved once, at ingest time, via a synchronous directory-service
-- lookup keyed by actor_account_id — never re-resolved on read (accepted staleness trade-off,
-- same as every other denormalized display name in this platform).
-- source_event_id is a deterministic string built from each upstream event's own natural key
-- (e.g. 'vote-cast:<voteRecordId>') since none of the consumed events carry a dedicated event id.
CREATE TABLE timeline_events (
  id                           uuid DEFAULT gen_random_uuid(),
  subject_account_id           uuid NOT NULL,
  type                         text NOT NULL,
  title                        text NOT NULL,
  detail                       text,
  occurred_at                  timestamptz NOT NULL,
  source_event_id              text NOT NULL,
  actor_account_id             uuid NOT NULL,
  actor_name_denormalized      text,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (type) REFERENCES timeline_event_type_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE timeline_events IS '"group" (Today/Yesterday/This week) is deliberately not a column — always computed from occurred_at at read time, same principle as legislative-service''s computed presence_rate/share.';
CREATE INDEX idx_timeline_events_subject ON timeline_events (subject_account_id, occurred_at DESC);
CREATE UNIQUE INDEX uq_timeline_events_source ON timeline_events (subject_account_id, source_event_id);

-- ---- Domain events published ----
-- ---- Domain events consumed ----
-- <- PostPublished (Feed & Content) — subject/actor = authorId; kind='video' maps to type=video, else type=post.
-- <- VoteCast (Legislative) — subject/actor = politicianAccountId.
-- <- LegislativeItemFiled (Legislative) — subject/actor = politicianAccountId; type = category (project/pec/cpi).
-- <- LegislativeItemStatusChanged (Legislative) — subject/actor = politicianAccountId; type = status_change.
-- <- CommitteeMembershipChanged (Legislative) — subject/actor = politicianAccountId.
-- <- PoliticianReassigned (Platform Configuration) — subject/actor = politicianAccountId; type = party_change.
-- <- FundraiserGoalReached (Fundraising) — subject/actor = organizerAccountId, resolved via a synchronous fundraising-service lookup by fundraiserId (the event itself only carries fundraiserId).
