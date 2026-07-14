-- ============================================================
-- Platform Configuration Service
-- Party legal registry, geography, languages, translation tags
-- Database: PostgreSQL 16 (+ in-memory/CDN cache on the read side)
-- Why: Tiny, slow-changing dataset read on every single request (nav labels, dropdowns) — correctness matters more than write throughput; the real performance story is the cache in front of it.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- System-of-record for a party's legal identity (acronym/number are electoral-court-issued). Directory Service holds the public-facing copy.
CREATE TABLE party_registry (
  id                           uuid DEFAULT gen_random_uuid(),
  name                         text NOT NULL,
  acronym                      text NOT NULL UNIQUE,
  number                       integer NOT NULL UNIQUE,
  president                    text,
  ideology                     text,
  member_count                 integer NOT NULL DEFAULT 0,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id)
);
COMMENT ON TABLE party_registry IS 'System-of-record for a party''s legal identity (acronym/number are electoral-court-issued). Directory Service holds the public-facing copy.';

-- Platform-admin-controlled party assignment, independent from how the politician was originally registered.
CREATE TABLE politician_assignments (
  politician_account_id        uuid,
  party_id                     uuid,
  updated_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (politician_account_id),
  FOREIGN KEY (party_id) REFERENCES party_registry (id)
);
COMMENT ON TABLE politician_assignments IS 'Platform-admin-controlled party assignment, independent from how the politician was originally registered.';

CREATE TABLE countries (
  id                           uuid DEFAULT gen_random_uuid(),
  name                         text NOT NULL,
  code                         char(2) NOT NULL UNIQUE,
  PRIMARY KEY (id)
);

CREATE TABLE states (
  id                           uuid DEFAULT gen_random_uuid(),
  country_id                   uuid NOT NULL,
  name                         text NOT NULL,
  code                         text NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (country_id) REFERENCES countries (id)
);
CREATE UNIQUE INDEX uq_state_country_code ON states (country_id, code);

CREATE TABLE languages (
  id                           text,
  name                         text NOT NULL,
  code                         text NOT NULL,
  is_default                   boolean NOT NULL DEFAULT false,
  PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uq_languages_one_default ON languages (is_default) WHERE is_default;
COMMENT ON COLUMN languages.id IS 'e.g. ''pt-br'', ''en-us''';

CREATE TABLE translation_keys (
  id                           uuid DEFAULT gen_random_uuid(),
  key                          text NOT NULL UNIQUE,
  PRIMARY KEY (id)
);

CREATE TABLE translation_values (
  translation_key_id           uuid,
  language_id                  text,
  value                        text NOT NULL,
  PRIMARY KEY (translation_key_id, language_id),
  FOREIGN KEY (translation_key_id) REFERENCES translation_keys (id),
  FOREIGN KEY (language_id) REFERENCES languages (id)
);

-- ---- Domain events published ----
-- -> PartyRegistered
-- -> PoliticianReassigned
-- -> LanguageAdded
-- -> LanguageRemoved
-- -> DefaultLanguageChanged
-- -> TranslationValueUpdated
-- -> CountryAdded
-- -> CountryRemoved
-- ---- Domain events consumed ----
-- <- PoliticianRegistered
