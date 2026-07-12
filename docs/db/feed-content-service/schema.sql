-- ============================================================
-- Feed & Content Service
-- Posts (text/agenda/live), likes, comments — the highest write+read QPS in the system
-- Database: PostgreSQL 16 (source of truth) + Redis (hot counters & timeline cache)
-- Why: A social feed is read orders of magnitude more than it is written. Postgres holds the durable Post aggregate; Redis serves the ranked timeline and like/comment counters so the hot path never runs a live join or COUNT(*).
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE post_kind_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE post_kind_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO post_kind_options (code, label, sort_order) VALUES
  ('text', 'Text', 1),
  ('agenda', 'Agenda', 2),
  ('live', 'Live', 3);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE post_visibility_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE post_visibility_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO post_visibility_options (code, label, sort_order) VALUES
  ('public', 'Public', 1),
  ('private', 'Private', 2);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
-- Shared UI severity vocabulary for post tags — duplicated locally in Party Management, which uses the same set for party-event tags.
CREATE TABLE tag_severity_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE tag_severity_options IS 'Shared UI severity vocabulary for post tags — duplicated locally in Party Management, which uses the same set for party-event tags.';
INSERT INTO tag_severity_options (code, label, sort_order) VALUES
  ('success', 'Success', 1),
  ('warning', 'Warning', 2),
  ('danger', 'Danger', 3),
  ('info', 'Info', 4),
  ('neutral', 'Neutral', 5),
  ('secondary', 'Secondary', 6),
  ('primary', 'Primary', 7);

-- engine: PostgreSQL, PARTITION BY RANGE (created_at)
-- Range-partitioned by created_at (monthly) — old partitions can be moved to cheaper storage without touching the hot one. created_at joins id in the primary key because Postgres requires the partition column in every unique constraint.
CREATE TABLE posts (
  id                           uuid DEFAULT gen_random_uuid(),
  author_account_id            uuid NOT NULL,
  kind                         text NOT NULL DEFAULT 'text',
  content                      text,
  image_url                    text,
  visibility                   text NOT NULL DEFAULT 'public',
  context                      text,
  live_session_id              uuid,
  created_at                   timestamptz DEFAULT now(),
  PRIMARY KEY (id, created_at),
  FOREIGN KEY (kind) REFERENCES post_kind_options (code) ON DELETE RESTRICT,
  FOREIGN KEY (visibility) REFERENCES post_visibility_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE posts IS 'Range-partitioned by created_at (monthly) — old partitions can be moved to cheaper storage without touching the hot one. created_at joins id in the primary key because Postgres requires the partition column in every unique constraint.';
CREATE INDEX idx_posts_author_created ON posts (author_account_id, created_at DESC);
COMMENT ON COLUMN posts.live_session_id IS 'reference into live-streaming-service.live_sessions.id — by value only';

-- No DB-level FK to posts: posts is partitioned by created_at, so Postgres can only enforce uniqueness/FKs against (id, created_at) together — referential integrity to the parent post is enforced at the application layer instead.
CREATE TABLE post_agenda_details (
  post_id                      uuid,
  title                        text NOT NULL,
  event_date                   text NOT NULL,
  location                     text NOT NULL,
  PRIMARY KEY (post_id)
);
COMMENT ON TABLE post_agenda_details IS 'No DB-level FK to posts: posts is partitioned by created_at, so Postgres can only enforce uniqueness/FKs against (id, created_at) together — referential integrity to the parent post is enforced at the application layer instead.';
COMMENT ON COLUMN post_agenda_details.event_date IS 'display label, e.g. "Aug 12, 2026 · 14:00"';

CREATE TABLE post_tags (
  id                           bigint GENERATED ALWAYS AS IDENTITY,
  post_id                      uuid NOT NULL,
  label                        text NOT NULL,
  severity                     text,
  icon                         text,
  PRIMARY KEY (id),
  FOREIGN KEY (severity) REFERENCES tag_severity_options (code) ON DELETE RESTRICT
);
CREATE INDEX idx_post_tags_post ON post_tags (post_id);
COMMENT ON COLUMN post_tags.post_id IS 'no DB-level FK — posts is partitioned, see post_agenda_details';

-- Hot table by design — expect Redis to absorb the write burst in front of this (see perf notes) with async flush.
CREATE TABLE likes (
  post_id                      uuid,
  account_id                   uuid,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (post_id, account_id)
);
COMMENT ON TABLE likes IS 'Hot table by design — expect Redis to absorb the write burst in front of this (see perf notes) with async flush.';

CREATE TABLE comments (
  id                           uuid DEFAULT gen_random_uuid(),
  post_id                      uuid NOT NULL,
  author_account_id            uuid NOT NULL,
  body                         text NOT NULL,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id)
);
CREATE INDEX idx_comments_post_created ON comments (post_id, created_at);

-- Denormalized counters, updated by an async consumer of PostLiked/CommentAdded — never computed with COUNT(*) on render. No DB-level FK — posts is partitioned, see post_agenda_details.
CREATE TABLE post_metrics (
  post_id                      uuid,
  likes_count                  integer NOT NULL DEFAULT 0,
  comments_count               integer NOT NULL DEFAULT 0,
  shares_count                 integer NOT NULL DEFAULT 0,
  updated_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (post_id)
);
COMMENT ON TABLE post_metrics IS 'Denormalized counters, updated by an async consumer of PostLiked/CommentAdded — never computed with COUNT(*) on render. No DB-level FK — posts is partitioned, see post_agenda_details.';

-- ---- Redis (not part of this relational schema, documented for ops) ----
-- feed:timeline:{account_id} — sorted set of post ids by rank score, the actual "Top/Latest/Following" read path; rebuilt async from PostPublished.
-- post:{id}:likes — fast counter, incremented synchronously on like; reconciled against the likes table periodically to correct drift.
-- feed:trending — sorted set powering the Trending Topics widget, decayed over time.

-- ---- Domain events published ----
-- -> PostPublished(post_id, author_id, kind, visibility)
-- -> PostLiked(post_id, account_id)
-- -> CommentAdded(post_id, comment_id)
-- ---- Domain events consumed ----
-- <- FollowCreated
-- <- FollowRemoved
