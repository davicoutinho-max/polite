-- ============================================================
-- Comment likes and one-level-deep replies — comments can now be liked (mirrors post likes,
-- but denormalized straight onto the comment row since comments never had a metrics table),
-- and a comment can carry a parent_comment_id to render as a reply. Unlike post_id/likes above,
-- comments is a normal (non-partitioned) table, so a real FK to it is possible here.
-- ============================================================

ALTER TABLE comments ADD COLUMN parent_comment_id uuid REFERENCES comments (id) ON DELETE CASCADE;
ALTER TABLE comments ADD COLUMN likes_count integer NOT NULL DEFAULT 0;
CREATE INDEX idx_comments_parent ON comments (parent_comment_id);

CREATE TABLE comment_likes (
  comment_id                   uuid REFERENCES comments (id) ON DELETE CASCADE,
  account_id                   uuid,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (comment_id, account_id)
);
