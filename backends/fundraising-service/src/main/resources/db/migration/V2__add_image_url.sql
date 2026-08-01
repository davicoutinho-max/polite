-- A fundraiser can now carry a cover image, uploaded through feed-content-service's shared
-- media endpoint the same way post/petition attachments are — see FundraiserService.create.
ALTER TABLE fundraisers ADD COLUMN image_url VARCHAR(2048);
