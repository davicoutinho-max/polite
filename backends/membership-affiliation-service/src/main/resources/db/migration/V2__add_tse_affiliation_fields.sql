-- Real Brazilian party-affiliation requests (per TSE Resolução 23.571/2018) require the citizen's
-- voter-registration data and a photo identity check, not just which party and which city — see
-- Affiliation domain model's javadoc.
ALTER TABLE affiliations ADD COLUMN voter_registration_number VARCHAR(20);
ALTER TABLE affiliations ADD COLUMN electoral_zone VARCHAR(10);
ALTER TABLE affiliations ADD COLUMN electoral_section VARCHAR(10);
ALTER TABLE affiliations ADD COLUMN electoral_state VARCHAR(2);
ALTER TABLE affiliations ADD COLUMN electoral_municipality VARCHAR(120);
ALTER TABLE affiliations ADD COLUMN identity_photo_url VARCHAR(2048);
