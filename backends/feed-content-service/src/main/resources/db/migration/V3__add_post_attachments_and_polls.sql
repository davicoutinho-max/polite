-- ============================================================
-- Post attachments (generic file, alongside the pre-existing image_url) and polls.
-- ============================================================

ALTER TABLE posts ADD COLUMN file_url text;
ALTER TABLE posts ADD COLUMN file_name text;

-- No DB-level FK to posts — posts is partitioned by created_at, see post_agenda_details for why.
-- A post "has a poll" simply by having rows here; the post's own content is the poll question.
CREATE TABLE post_poll_options (
  id                           uuid DEFAULT gen_random_uuid(),
  post_id                      uuid NOT NULL,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);
CREATE INDEX idx_post_poll_options_post ON post_poll_options (post_id, sort_order);

-- One row per (post, account) — voting for a different option updates this row rather than
-- inserting a second one, so an account can change its vote but never hold two at once.
CREATE TABLE post_poll_votes (
  post_id                      uuid NOT NULL,
  account_id                   uuid NOT NULL,
  option_id                    uuid NOT NULL,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (post_id, account_id)
);
CREATE INDEX idx_post_poll_votes_option ON post_poll_votes (option_id);
