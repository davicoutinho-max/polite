-- ============================================================
-- Cross-posting to external social networks — a politician/party connects their real
-- Facebook Page, Instagram Business account and/or X account (OAuth), then can publish a post
-- to whichever of those are connected. See SocialConnectionController/SocialShareController.
-- ============================================================

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE social_platform_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE social_platform_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO social_platform_options (code, label, sort_order) VALUES
  ('facebook', 'Facebook', 1),
  ('instagram', 'Instagram', 2),
  ('x', 'X', 3);

-- One connection per account per platform — reconnecting overwrites the previous token rather
-- than accumulating rows (see SocialConnection.reconnect's javadoc).
CREATE TABLE social_connections (
  id                           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  account_id                   uuid NOT NULL,
  platform                     text NOT NULL REFERENCES social_platform_options (code) ON DELETE RESTRICT,
  access_token                 text NOT NULL,
  external_account_id          text NOT NULL,
  external_account_name        text,
  connected_at                 timestamptz NOT NULL DEFAULT now(),
  UNIQUE (account_id, platform)
);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE social_share_status_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
INSERT INTO social_share_status_options (code, label, sort_order) VALUES
  ('published', 'Published', 1),
  ('failed', 'Failed', 2);

CREATE TABLE social_shares (
  id                           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  post_id                      uuid NOT NULL,
  platform                     text NOT NULL REFERENCES social_platform_options (code) ON DELETE RESTRICT,
  status                       text NOT NULL REFERENCES social_share_status_options (code) ON DELETE RESTRICT,
  external_post_id             text,
  error_message                text,
  shared_at                    timestamptz NOT NULL DEFAULT now()
);
COMMENT ON COLUMN social_shares.post_id IS 'no DB-level FK — posts is partitioned, see post_agenda_details';
CREATE INDEX idx_social_shares_post ON social_shares (post_id);
