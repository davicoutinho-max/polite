-- ============================================================
-- A citizen's own, personal record of who they voted for in each race of an election. This is
-- explicitly NOT the official ballot — Brazil's constitution guarantees vote secrecy, and this
-- table only ever stores what a citizen chooses to self-report, purely for their own history/
-- reflection. The frontend must always present this as personal/unofficial (see the vote-register
-- screen's own disclaimer copy).
-- ============================================================

CREATE TABLE personal_votes (
  id                       uuid DEFAULT gen_random_uuid(),
  citizen_account_id       uuid NOT NULL,
  election_id              uuid NOT NULL,
  office                   text NOT NULL,
  candidate_name           text NOT NULL,
  candidate_party_acronym  text,
  politician_account_id    uuid,
  cast_at                  timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (election_id) REFERENCES elections (id)
);
COMMENT ON TABLE personal_votes IS 'Personal/unofficial self-reported vote history — never the real secret ballot.';

-- One personal-vote record per citizen per (election, office) — re-registering the same office
-- updates the existing pick rather than accumulating duplicates (a citizen changing their mind
-- before the record is "final" in their own head, or just re-confirming).
CREATE UNIQUE INDEX uq_personal_votes_citizen_election_office ON personal_votes (citizen_account_id, election_id, office);
