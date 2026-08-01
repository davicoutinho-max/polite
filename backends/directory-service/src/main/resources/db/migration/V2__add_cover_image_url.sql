-- Politicians can now upload their own cover image (in addition to the existing avatar_url) via
-- a direct self-service update — see UpdateProfileImagesService. Parties already have a real
-- cover_url column of their own in party-management-service's party_profiles table (previously
-- unused by the frontend, now wired up) — no equivalent column needed here.
ALTER TABLE politicians ADD COLUMN cover_image_url VARCHAR(2048);
