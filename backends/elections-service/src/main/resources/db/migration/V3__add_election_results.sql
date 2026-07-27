-- ============================================================
-- Real per-candidate vote tallies (winner/2nd/3rd/... and their vote counts), sourced from TSE's
-- votacao_candidato_munzona dataset via government-sync-service. Deliberately NOT modeled as an
-- extension of election_candidacies: most candidates in a race never get a platform account at
-- all (only the ones government-sync-service already syncs as politicians do), so the
-- candidate's name/party/vote-count are stored directly here instead of requiring a linked
-- account.
-- ============================================================

CREATE TABLE election_results (
  id                     uuid DEFAULT gen_random_uuid(),
  election_id            uuid NOT NULL,
  office                 text NOT NULL,
  external_id            text NOT NULL,
  candidate_name         text NOT NULL,
  party_acronym          text,
  votes                  bigint NOT NULL,
  rank                   integer NOT NULL,
  elected                boolean NOT NULL DEFAULT false,
  politician_account_id  uuid,
  PRIMARY KEY (id),
  FOREIGN KEY (election_id) REFERENCES elections (id)
);
COMMENT ON COLUMN election_results.politician_account_id IS 'set only when this candidate was also synced as a platform politician (elected candidates); resolved against Directory Service at query time like election_candidacies, never replicated beyond the id';

CREATE UNIQUE INDEX uq_election_results_candidate ON election_results (election_id, office, external_id);
CREATE INDEX idx_election_results_rank ON election_results (election_id, office, rank);
