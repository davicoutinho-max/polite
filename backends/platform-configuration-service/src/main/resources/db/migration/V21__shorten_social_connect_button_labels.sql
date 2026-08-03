-- The social-connections page moved from per-platform cards to a flat list (row already shows
-- the platform name/icon), so "Connect with Facebook"/"Connect with X" next to the row's own
-- "Facebook"/"X" label read as redundant repetition — shortened to just "Connect".

UPDATE translation_values
SET value = 'Connect'
WHERE language_id = 'en-us'
  AND translation_key_id IN (SELECT id FROM translation_keys WHERE key IN ('button.connect-meta', 'button.connect-x'));

UPDATE translation_values
SET value = 'Conectar'
WHERE language_id = 'pt-br'
  AND translation_key_id IN (SELECT id FROM translation_keys WHERE key IN ('button.connect-meta', 'button.connect-x'));
