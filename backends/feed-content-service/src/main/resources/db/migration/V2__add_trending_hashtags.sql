-- Regex-extracted #hashtag tokens from Post.content at publish time (TEXT posts only — content
-- is null for AGENDA/LIVE). No DB-level FK to posts — same partitioning reason as post_tags.
CREATE TABLE post_hashtags (
  id                           bigint GENERATED ALWAYS AS IDENTITY,
  post_id                      uuid NOT NULL,
  hashtag                      text NOT NULL,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id)
);
COMMENT ON COLUMN post_hashtags.post_id IS 'no DB-level FK — posts is partitioned, see post_agenda_details';
COMMENT ON COLUMN post_hashtags.created_at IS 'copied from the post''s own publish time at extraction — lets the 24h trending window be computed here without joining the partitioned posts table.';
CREATE INDEX idx_post_hashtags_hashtag_created ON post_hashtags (hashtag, created_at);
CREATE INDEX idx_post_hashtags_post ON post_hashtags (post_id);

-- Recomputed periodically by a @Scheduled job from post_hashtags — never trusted as an
-- independently-writable table; GET /trending only ever reads this cache.
CREATE TABLE trending_topics_cache (
  hashtag                      text,
  post_count_last_24h          integer NOT NULL,
  rank                         smallint NOT NULL,
  computed_at                  timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (hashtag)
);
COMMENT ON TABLE trending_topics_cache IS 'Recomputed periodically from post_hashtags by a @Scheduled job — never written to directly by request-handling code.';
