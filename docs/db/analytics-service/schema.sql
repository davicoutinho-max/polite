-- ============================================================
-- Analytics Service
-- Per-author engagement dashboard — pure consumer read-model over Feed & Content and Directory
-- events. Publishes nothing.
-- Database: PostgreSQL 16
-- Why: the frontend mock's "byDistrict" dimension has no real backing field anywhere in the
-- platform — no citizen account carries a state/district (only politicians carry the state
-- *they represent*, and party-management-service's optional per-affiliation `city` is the
-- closest real citizen-linked location field, but it's free-text, optional, and scoped to party
-- membership only). Rather than fake a geography dimension, this service reports engagement by
-- the ENGAGING ACCOUNT'S TYPE (citizen/politician/party/admin) instead — a real, always-populated
-- field on every account — see identity-service's Account.accountType.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE engagement_event_type_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE engagement_event_type_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO engagement_event_type_options (code, label, sort_order) VALUES
  ('post_published', 'Post published', 1),
  ('like', 'Like', 2),
  ('comment', 'Comment', 3),
  ('follow_created', 'Follow gained', 4),
  ('follow_removed', 'Follow lost', 5);

-- Raw per-interaction fact table — every KPI (reach, engagement rate, by-content-type,
-- by-account-type) is a query against this table, computed at read time; "demo-scale traffic"
-- makes a pre-aggregated daily-counter cache unnecessary for this pass (same reasoning already
-- applied to the author-display-data lookup below).
-- actor_account_type is resolved once, at ingest time, via a synchronous identity-service lookup
-- keyed by actor_account_id — never re-resolved on read (same denormalization trade-off as
-- activity-feed-service's actor_name_denormalized).
-- content_type mirrors feed-content-service's Post.kind (text/agenda/live) and is only populated
-- for post_published/like/comment events — resolved via a synchronous feed-content-service
-- lookup when the consumed event doesn't already carry it (PostLiked/CommentAdded only carry
-- postId/commentId, not the post's author or kind).
-- source_event_id is a deterministic string built from each upstream event's own natural key
-- since none of the consumed events carry a dedicated event id.
CREATE TABLE engagement_events (
  id                           bigint GENERATED ALWAYS AS IDENTITY,
  author_account_id            uuid NOT NULL,
  actor_account_id             uuid NOT NULL,
  actor_account_type           text,
  event_type                   text NOT NULL,
  content_type                 text,
  occurred_at                  timestamptz NOT NULL,
  source_event_id              text NOT NULL,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (event_type) REFERENCES engagement_event_type_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE engagement_events IS 'Raw per-interaction fact table — every read (kpis/engagement/by-content-type/by-account-type) aggregates this table at query time; demo-scale traffic makes a pre-aggregated cache unnecessary for this pass.';
CREATE INDEX idx_engagement_events_author_occurred ON engagement_events (author_account_id, occurred_at DESC);
CREATE INDEX idx_engagement_events_author_type ON engagement_events (author_account_id, event_type);
CREATE UNIQUE INDEX uq_engagement_events_source ON engagement_events (author_account_id, source_event_id);

-- ---- Domain events published ----
-- ---- Domain events consumed ----
-- <- PostPublished (Feed & Content) — author/actor = authorId; content_type = kind (already on the event).
-- <- PostLiked (Feed & Content) — actor = accountId; author/content_type resolved via a synchronous feed-content-service lookup on postId.
-- <- CommentAdded (Feed & Content) — author/content_type resolved via a synchronous feed-content-service lookup on postId; actor (commenter) resolved via the same service's comment list.
-- <- FollowCreated / FollowRemoved (Directory) — author = targetId, actor = followerAccountId; content_type is null (not content-related).
