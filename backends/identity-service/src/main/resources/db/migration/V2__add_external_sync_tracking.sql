-- ============================================================
-- Supports government-data-sync-provisioned accounts (see government-sync-service, Account.
-- registerSynced): a real deputy/senator/party never signed up and has no password, but still
-- needs a real accountId so directory/messaging/feed can resolve it like any other account.
-- external_source/external_id together are the sync job's idempotency key (re-running the job
-- updates the existing row instead of duplicating it) — both null for every account that came
-- through the normal signup/provisioning path.
-- ============================================================

ALTER TABLE accounts ADD COLUMN external_source text;
ALTER TABLE accounts ADD COLUMN external_id text;

CREATE UNIQUE INDEX uq_accounts_external_source_id ON accounts (external_source, external_id)
  WHERE external_source IS NOT NULL;
