-- Politician profile-header avatar/cover upload had no success/error feedback at all — a click
-- that silently succeeded (or silently failed) looked identical to the user, easily read as "this
-- button doesn't do anything". Added real toasts; these are the translation keys they need.
-- title.cover-updated/hint.cover-updated also cover the party page's own cover-upload toast,
-- added earlier without a migration.

INSERT INTO translation_keys (key) VALUES
  ('title.photo-updated'),
  ('hint.photo-updated'),
  ('title.cover-updated'),
  ('hint.cover-updated'),
  ('title.upload-failed'),
  ('hint.upload-failed');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('title.photo-updated', 'en-us', 'Profile photo updated'),
  ('title.photo-updated', 'pt-br', 'Foto de perfil atualizada'),
  ('hint.photo-updated', 'en-us', 'Your new photo is now public.'),
  ('hint.photo-updated', 'pt-br', 'Sua nova foto já está pública.'),
  ('title.cover-updated', 'en-us', 'Cover photo updated'),
  ('title.cover-updated', 'pt-br', 'Foto de capa atualizada'),
  ('hint.cover-updated', 'en-us', 'Your new cover photo is now public.'),
  ('hint.cover-updated', 'pt-br', 'Sua nova foto de capa já está pública.'),
  ('title.upload-failed', 'en-us', 'Upload failed'),
  ('title.upload-failed', 'pt-br', 'Falha no envio'),
  ('hint.upload-failed', 'en-us', 'Please try again shortly.'),
  ('hint.upload-failed', 'pt-br', 'Tente novamente em instantes.')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
