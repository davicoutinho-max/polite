-- Time-boxed, single-use invites that let someone self-register as a party or politician account
-- instead of the default citizen — replaces the old "admin/party types the new account's password
-- directly" flows. See RegistrationToken's javadoc.

CREATE TABLE registration_tokens (
  id                           uuid,
  token                        text NOT NULL,
  account_type                 text NOT NULL,
  issued_by_account_id         uuid NOT NULL,
  target_email                 text,
  prefill_data_json            text,
  created_at                   timestamptz NOT NULL DEFAULT now(),
  expires_at                   timestamptz NOT NULL,
  consumed_at                  timestamptz,
  invalidated                  boolean NOT NULL DEFAULT false,
  PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uq_registration_tokens_token ON registration_tokens (token);
CREATE INDEX idx_registration_tokens_issued_by ON registration_tokens (issued_by_account_id);
