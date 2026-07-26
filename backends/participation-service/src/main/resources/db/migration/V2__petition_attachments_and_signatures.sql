ALTER TABLE petitions ADD COLUMN image_url text;
ALTER TABLE petitions ADD COLUMN video_url text;
ALTER TABLE petitions ADD COLUMN file_url text;
ALTER TABLE petitions ADD COLUMN file_name text;
ALTER TABLE petitions ADD COLUMN petition_type text NOT NULL DEFAULT 'verified_support';

ALTER TABLE petition_signatures ADD COLUMN full_name text;
ALTER TABLE petition_signatures ADD COLUMN cpf text;
ALTER TABLE petition_signatures ADD COLUMN birth_date date;
ALTER TABLE petition_signatures ADD COLUMN city text;
ALTER TABLE petition_signatures ADD COLUMN state text;
ALTER TABLE petition_signatures ADD COLUMN verification_method text;
ALTER TABLE petition_signatures ADD COLUMN electoral_data text;
ALTER TABLE petition_signatures ADD COLUMN e_signature_consent boolean NOT NULL DEFAULT false;
ALTER TABLE petition_signatures ADD COLUMN identity_validated boolean NOT NULL DEFAULT false;
ALTER TABLE petition_signatures ADD COLUMN typed_signature text;

-- Ephemeral pending-signature records: created by "start signature", consumed (or expired) by
-- "confirm signature". Carries every field captured in step 1 so the final petition_signatures
-- row can be materialized in one shot once the code checks out, without a server-side session.
CREATE TABLE petition_signature_verifications (
  id                    uuid DEFAULT gen_random_uuid(),
  petition_id           uuid NOT NULL,
  citizen_account_id    uuid NOT NULL,
  code                  text NOT NULL,
  contact               text,
  method                text NOT NULL,
  full_name             text NOT NULL,
  cpf                   text NOT NULL,
  birth_date            date,
  city                  text,
  state                 text,
  electoral_data        text,
  e_signature_consent   boolean NOT NULL DEFAULT false,
  typed_signature       text NOT NULL,
  expires_at            timestamptz NOT NULL,
  consumed              boolean NOT NULL DEFAULT false,
  created_at            timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  FOREIGN KEY (petition_id) REFERENCES petitions (id) ON DELETE CASCADE
);
