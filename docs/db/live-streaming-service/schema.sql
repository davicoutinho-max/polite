-- ============================================================
-- Live Streaming Service
-- Live session lifecycle, viewer presence and in-stream chat — the most latency-sensitive service in the system
-- Database: PostgreSQL 16 (session metadata) + Redis (presence/viewer-count) + Kafka topic (chat fan-out, short retention)
-- Why: Session start/end is low-volume and transactional (Postgres); viewer presence must be O(1) reads under constant churn (Redis); live chat is bursty, ephemeral and fans out to thousands of viewers at once (pub/sub over a durable relational table).
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE live_session_status_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE live_session_status_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO live_session_status_options (code, label, sort_order) VALUES
  ('scheduled', 'Scheduled', 1),
  ('live', 'Live', 2),
  ('ended', 'Ended', 3);

CREATE TABLE live_sessions (
  id                           uuid DEFAULT gen_random_uuid(),
  host_account_id              uuid NOT NULL,
  post_id                      uuid,
  video_id                     text,
  channel_id                   text,
  status                       text NOT NULL DEFAULT 'scheduled',
  scheduled_for                timestamptz,
  started_at                   timestamptz,
  ended_at                     timestamptz,
  peak_viewers                 integer NOT NULL DEFAULT 0,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (status) REFERENCES live_session_status_options (code) ON DELETE RESTRICT
);
CREATE INDEX idx_live_sessions_status ON live_sessions (status) WHERE status = 'live';
COMMENT ON COLUMN live_sessions.post_id IS 'reference into feed-content-service.posts.id, set once the live post is published';

-- Post-session rollup, written once when a session transitions to ended — never updated during the live window.
CREATE TABLE live_session_stats (
  live_session_id              uuid,
  total_unique_viewers         integer NOT NULL DEFAULT 0,
  total_chat_messages          integer NOT NULL DEFAULT 0,
  avg_watch_seconds            integer,
  computed_at                  timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (live_session_id),
  FOREIGN KEY (live_session_id) REFERENCES live_sessions (id) ON DELETE CASCADE
);
COMMENT ON TABLE live_session_stats IS 'Post-session rollup, written once when a session transitions to ended — never updated during the live window.';

-- OPTIONAL cold table — only populated if moderation/compliance requires retaining chat beyond the Kafka topic's retention window. Chat during the live window itself never touches this table.
CREATE TABLE live_chat_archive (
  id                           bigint GENERATED ALWAYS AS IDENTITY,
  live_session_id              uuid NOT NULL,
  account_id                   uuid NOT NULL,
  body                         text NOT NULL,
  sent_at                      timestamptz NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (live_session_id) REFERENCES live_sessions (id) ON DELETE CASCADE
);
COMMENT ON TABLE live_chat_archive IS 'OPTIONAL cold table — only populated if moderation/compliance requires retaining chat beyond the Kafka topic''s retention window. Chat during the live window itself never touches this table.';
CREATE INDEX idx_chat_archive_session ON live_chat_archive (live_session_id, sent_at);

-- ---- Redis (not part of this relational schema, documented for ops) ----
-- live:{session_id}:viewers — SET of account_ids currently connected, refreshed by a client heartbeat with a short TTL so disconnects self-expire.
-- live:{session_id}:viewer_count — INCR/DECR integer, the number the UI polls; never derived from SCARD on the hot path at high viewer counts.

-- ---- Kafka (not part of this relational schema, documented for ops) ----
-- live-chat-messages topic, partitioned by live_session_id, short retention (24–72h). A WebSocket gateway subscribes and fans out to connected viewers — chat never round-trips through Postgres per message.

-- ---- Domain events published ----
-- -> LiveSessionScheduled
-- -> LiveSessionStarted
-- -> LiveSessionEnded
-- ---- Domain events consumed ----
