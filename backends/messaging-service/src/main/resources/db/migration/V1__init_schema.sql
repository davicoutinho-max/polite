-- ============================================================
-- Messaging Service
-- Direct messages and group chat — politicians/parties can start groups, citizens reply only
-- Database: PostgreSQL 16, hash-partitioned by conversation_id (scale-out path to Cassandra/ScyllaDB for message history)
-- Why: Message volume grows unboundedly with engagement. Postgres partitioned by conversation gets a social network a long way; the documented scale-out path is a wide-column store keyed the same way, without changing the access pattern the app already uses.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE conversations (
  id                           uuid DEFAULT gen_random_uuid(),
  is_group                     boolean NOT NULL DEFAULT false,
  group_name                   text,
  group_avatar_url             text,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  last_message_at              timestamptz,
  PRIMARY KEY (id)
);
CREATE INDEX idx_conversations_last_message ON conversations (last_message_at DESC);

-- Group membership is restricted to politician/party account types at the application layer — the picker never offers citizens as addable participants.
CREATE TABLE conversation_participants (
  conversation_id              uuid,
  account_id                   uuid,
  joined_at                    timestamptz NOT NULL DEFAULT now(),
  last_read_at                 timestamptz,
  PRIMARY KEY (conversation_id, account_id),
  FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE
);
COMMENT ON TABLE conversation_participants IS 'Group membership is restricted to politician/party account types at the application layer — the picker never offers citizens as addable participants.';
CREATE INDEX idx_participants_account ON conversation_participants (account_id);

-- engine: PostgreSQL, PARTITION BY HASH (conversation_id) — 32 partitions
CREATE TABLE messages (
  id                           uuid DEFAULT gen_random_uuid(),
  conversation_id              uuid,
  sender_account_id            uuid NOT NULL,
  body                         text NOT NULL,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id, conversation_id)
) PARTITION BY HASH (conversation_id);
CREATE INDEX idx_messages_conversation_created ON messages (conversation_id, created_at);
CREATE TABLE messages_p0 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 0);
CREATE TABLE messages_p1 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 1);
CREATE TABLE messages_p2 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 2);
CREATE TABLE messages_p3 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 3);
CREATE TABLE messages_p4 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 4);
CREATE TABLE messages_p5 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 5);
CREATE TABLE messages_p6 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 6);
CREATE TABLE messages_p7 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 7);
CREATE TABLE messages_p8 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 8);
CREATE TABLE messages_p9 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 9);
CREATE TABLE messages_p10 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 10);
CREATE TABLE messages_p11 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 11);
CREATE TABLE messages_p12 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 12);
CREATE TABLE messages_p13 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 13);
CREATE TABLE messages_p14 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 14);
CREATE TABLE messages_p15 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 15);
CREATE TABLE messages_p16 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 16);
CREATE TABLE messages_p17 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 17);
CREATE TABLE messages_p18 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 18);
CREATE TABLE messages_p19 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 19);
CREATE TABLE messages_p20 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 20);
CREATE TABLE messages_p21 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 21);
CREATE TABLE messages_p22 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 22);
CREATE TABLE messages_p23 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 23);
CREATE TABLE messages_p24 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 24);
CREATE TABLE messages_p25 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 25);
CREATE TABLE messages_p26 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 26);
CREATE TABLE messages_p27 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 27);
CREATE TABLE messages_p28 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 28);
CREATE TABLE messages_p29 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 29);
CREATE TABLE messages_p30 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 30);
CREATE TABLE messages_p31 PARTITION OF messages FOR VALUES WITH (modulus 32, remainder 31);

-- ---- Redis (not part of this relational schema, documented for ops) ----
-- presence:{account_id} — online/offline + last-seen, ephemeral with TTL heartbeat.
-- typing:{conversation_id} — SET of currently-typing account_ids, TTL a few seconds.
-- unread:{account_id}:{conversation_id} — counter, source of the badge; reconciled against conversation_participants.last_read_at.

-- ---- Domain events published ----
-- -> ConversationCreated
-- -> MessageSent
-- ---- Domain events consumed ----
