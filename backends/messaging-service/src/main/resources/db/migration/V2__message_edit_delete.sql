-- ============================================================
-- Message editing and soft delete (WhatsApp-style: "Edited" tag, tombstone on delete).
-- `body NOT NULL` is relaxed since a deleted message clears its body entirely rather than just
-- hiding it — ALTER TABLE on the partitioned parent cascades to every messages_p* partition.
-- ============================================================

ALTER TABLE messages ALTER COLUMN body DROP NOT NULL;
ALTER TABLE messages ADD COLUMN edited_at timestamptz;
ALTER TABLE messages ADD COLUMN deleted_at timestamptz;
