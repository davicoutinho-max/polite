-- ============================================================
-- Identity & Access Service
-- Accounts, credentials, sessions, role → permission derivation
-- Database: PostgreSQL 16
-- Why: Auth is low-volume, high-integrity — ACID and row-level locking matter far more than raw throughput here.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE account_type_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE account_type_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO account_type_options (code, label, sort_order) VALUES
  ('citizen', 'Citizen', 1),
  ('politician', 'Politician', 2),
  ('party', 'Party', 3),
  ('admin', 'Platform Admin', 4);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE document_type_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE document_type_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO document_type_options (code, label, sort_order) VALUES
  ('cpf', 'CPF', 1),
  ('cnpj', 'CNPJ', 2);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
CREATE TABLE document_status_options (
  code                         text,
  label                        text NOT NULL,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);
COMMENT ON TABLE document_status_options IS 'Parameter table — add a row here to introduce a new value, no schema migration required.';
INSERT INTO document_status_options (code, label, sort_order) VALUES
  ('pending', 'Pending', 1),
  ('verified', 'Verified', 2),
  ('rejected', 'Rejected', 3);

-- One row per authenticatable identity. Rich profile data (office, party, bio) is owned by Directory/Party Management, not here.
CREATE TABLE accounts (
  id                           uuid DEFAULT gen_random_uuid(),
  account_type                 text NOT NULL,
  name                         text NOT NULL,
  handle                       citext NOT NULL UNIQUE,
  email                        citext NOT NULL UNIQUE,
  password_hash                text NOT NULL,
  document_type                text,
  document_number_hash         text UNIQUE,
  document_number_encrypted    bytea,
  verified                     boolean NOT NULL DEFAULT false,
  anonymized_at                timestamptz,
  avatar_url                   text,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  updated_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (account_type) REFERENCES account_type_options (code) ON DELETE RESTRICT,
  FOREIGN KEY (document_type) REFERENCES document_type_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE accounts IS 'One row per authenticatable identity. Rich profile data (office, party, bio) is owned by Directory/Party Management, not here.';
CREATE INDEX idx_accounts_type ON accounts (account_type);
COMMENT ON COLUMN accounts.document_type IS 'null only for role=admin';
COMMENT ON COLUMN accounts.document_number_hash IS 'SHA-256 of normalized CPF/CNPJ, for uniqueness checks';
COMMENT ON COLUMN accounts.document_number_encrypted IS 'envelope-encrypted (KMS); raw document never stored in plaintext';
COMMENT ON COLUMN accounts.anonymized_at IS 'Set once Privacy & Compliance completes the erasure saga. name/email/document_* are overwritten with placeholders at that point instead of deleting the row — other services still hold this id in posts/comments/messages and must keep resolving it.';

-- Seeded: citizen, politician, party, admin. Kept as data (not an enum-only check) so permissions can change without a redeploy.
CREATE TABLE roles (
  id                           smallint,
  name                         text NOT NULL UNIQUE,
  PRIMARY KEY (id),
  FOREIGN KEY (name) REFERENCES account_type_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE roles IS 'Seeded: citizen, politician, party, admin. Kept as data (not an enum-only check) so permissions can change without a redeploy.';

-- Mirrors today's static TYPE_PERMISSIONS map, now editable data.
CREATE TABLE role_permissions (
  role_id                      smallint,
  permission                   text,
  PRIMARY KEY (role_id, permission),
  FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);
COMMENT ON TABLE role_permissions IS 'Mirrors today''s static TYPE_PERMISSIONS map, now editable data.';

-- Refresh-token tracking so sessions are revocable (logout-everywhere, compromised-token response).
CREATE TABLE sessions (
  id                           uuid DEFAULT gen_random_uuid(),
  account_id                   uuid NOT NULL,
  refresh_token_hash           text NOT NULL UNIQUE,
  user_agent                   text,
  ip_address                   inet,
  issued_at                    timestamptz NOT NULL DEFAULT now(),
  expires_at                   timestamptz NOT NULL,
  revoked_at                   timestamptz,
  PRIMARY KEY (id),
  FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
);
COMMENT ON TABLE sessions IS 'Refresh-token tracking so sessions are revocable (logout-everywhere, compromised-token response).';
CREATE INDEX idx_sessions_account ON sessions (account_id);
CREATE INDEX idx_sessions_expiry ON sessions (expires_at);
COMMENT ON COLUMN sessions.ip_address IS 'Collected for security/fraud response under legitimate-interest — purged by the same retention job that clears expired sessions, not kept indefinitely.';

-- Audit trail of calls through the CpfVerificationGateway anti-corruption layer (KYC/Receita Federal in production).
CREATE TABLE document_verification_attempts (
  id                           uuid DEFAULT gen_random_uuid(),
  account_id                   uuid NOT NULL,
  document_type                text NOT NULL,
  status                       text NOT NULL,
  provider                     text NOT NULL,
  provider_ref                 text,
  checked_at                   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE,
  FOREIGN KEY (document_type) REFERENCES document_type_options (code) ON DELETE RESTRICT,
  FOREIGN KEY (status) REFERENCES document_status_options (code) ON DELETE RESTRICT
);
COMMENT ON TABLE document_verification_attempts IS 'Audit trail of calls through the CpfVerificationGateway anti-corruption layer (KYC/Receita Federal in production).';

-- ---- Domain events published ----
-- -> AccountRegistered(account_id, account_type, document_hash)
-- -> SessionIssued(session_id, account_id)
-- -> SessionRevoked(session_id)
-- -> AccountVerified(account_id)
-- ---- Domain events consumed ----
