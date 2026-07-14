-- ============================================================
-- Assistant Service
-- Canned civic-explainer answers per legislative topic — DB-backed, explicitly NOT LLM-backed
-- (see the frontend mock's own scope note: every response is deterministic, never legal advice).
-- Database: PostgreSQL 16
-- Why: wiring a live LLM is a separate, materially different future decision (API keys, cost,
-- prompt/safety design) — this pass only replaces the frontend's hardcoded array with a real,
-- editable data store behind the same static-answer UX.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE assistant_prompt_kind_options (
  code                         text,
  label                        text NOT NULL,
  icon                         text,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE assistant_prompt_kind_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO assistant_prompt_kind_options (code, label, icon, sort_order) VALUES
  ('summary', 'Summarize', 'summarize', 1),
  ('plain', 'Explain simply', 'lightbulb', 2),
  ('impact', 'What changes if approved?', 'trending_up', 3);

-- legislative_item_id is nullable and carries no DB-level FK — this service has no reason to
-- depend on legislative-service being reachable just to list its own static topics; the link is
-- informational only (kept in sync by whoever seeds this table).
CREATE TABLE assistant_topics (
  id                           uuid DEFAULT gen_random_uuid(),
  reference                    text NOT NULL,
  title                        text NOT NULL,
  legislative_item_id          uuid,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id)
);
COMMENT ON COLUMN assistant_topics.legislative_item_id IS 'informational only, no DB-level FK — this service does not depend on legislative-service being reachable to list its own static topics.';

CREATE TABLE assistant_answers (
  id                           bigint GENERATED ALWAYS AS IDENTITY,
  topic_id                     uuid NOT NULL,
  prompt_kind                  text NOT NULL,
  answer_text                  text NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (topic_id) REFERENCES assistant_topics (id) ON DELETE CASCADE,
  FOREIGN KEY (prompt_kind) REFERENCES assistant_prompt_kind_options (code) ON DELETE RESTRICT,
  UNIQUE (topic_id, prompt_kind)
);
CREATE INDEX idx_assistant_answers_topic ON assistant_answers (topic_id);

-- ---- Domain events published ----
-- ---- Domain events consumed ----
-- (none — no Kafka involvement; over-engineering for a handful of canned strings)
