-- ============================================================
-- Participation Service
-- Petitions, consultations and surveys — opinion instruments, not official votes
-- Database: PostgreSQL 16
-- Why: Simple CRUD-plus-idempotency workload; the interesting constraint is "one action per citizen per item," enforced at the DB layer via composite primary keys.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE consultation_stance_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE consultation_stance_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO consultation_stance_options (code, label, sort_order) VALUES
  ('favor', 'In favor', 1),
  ('against', 'Against', 2),
  ('neutral', 'Neutral', 3);

CREATE TABLE petitions (
  id                           uuid DEFAULT gen_random_uuid(),
  title                        text NOT NULL,
  summary                      text,
  category                     text,
  goal                         integer NOT NULL,
  signatures_count             integer NOT NULL DEFAULT 0,
  deadline                     date,
  PRIMARY KEY (id)
);

CREATE TABLE petition_signatures (
  petition_id                  uuid,
  citizen_account_id           uuid,
  signed_at                    timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (petition_id, citizen_account_id),
  FOREIGN KEY (petition_id) REFERENCES petitions (id) ON DELETE CASCADE
);
CREATE INDEX idx_petition_signatures_citizen ON petition_signatures (citizen_account_id);

CREATE TABLE consultations (
  id                           uuid DEFAULT gen_random_uuid(),
  title                        text NOT NULL,
  description                  text,
  deadline                     date,
  responses_count              integer NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- A citizen may change their stance — updates in place, never double-counts responses_count on the second write.
CREATE TABLE consultation_responses (
  consultation_id              uuid,
  citizen_account_id           uuid,
  stance                       text NOT NULL,
  updated_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (consultation_id, citizen_account_id),
  FOREIGN KEY (consultation_id) REFERENCES consultations (id) ON DELETE CASCADE,
  FOREIGN KEY (stance) REFERENCES consultation_stance_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE consultation_responses IS 'A citizen may change their stance — updates in place, never double-counts responses_count on the second write.';
CREATE INDEX idx_consultation_responses_citizen ON consultation_responses (citizen_account_id);

CREATE TABLE surveys (
  id                           uuid DEFAULT gen_random_uuid(),
  question                     text NOT NULL,
  context                      text,
  PRIMARY KEY (id)
);

CREATE TABLE survey_options (
  id                           uuid DEFAULT gen_random_uuid(),
  survey_id                    uuid NOT NULL,
  label                        text NOT NULL,
  votes_count                  integer NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  FOREIGN KEY (survey_id) REFERENCES surveys (id) ON DELETE CASCADE
);
CREATE INDEX idx_survey_options_survey ON survey_options (survey_id);

CREATE TABLE survey_votes (
  survey_id                    uuid,
  citizen_account_id           uuid,
  option_id                    uuid NOT NULL,
  voted_at                     timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (survey_id, citizen_account_id),
  FOREIGN KEY (survey_id) REFERENCES surveys (id) ON DELETE CASCADE,
  FOREIGN KEY (option_id) REFERENCES survey_options (id) ON DELETE CASCADE
);
CREATE INDEX idx_survey_votes_citizen ON survey_votes (citizen_account_id);

-- ---- Domain events published ----
-- -> PetitionSigned
-- -> ConsultationStanceSet
-- -> SurveyVoteCast
-- ---- Domain events consumed ----
