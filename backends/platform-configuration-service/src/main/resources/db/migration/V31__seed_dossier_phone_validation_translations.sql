-- The edit-profile dossier form's phone field had no format validation at all — anything typed
-- there (including plain free text) saved straight through as "phone". Added real validation
-- (see EditProfilePage's phonePattern) plus these two new strings; error.invalid-email already
-- existed from V5, reused as-is.

INSERT INTO translation_keys (key) VALUES ('error.invalid-phone'), ('placeholder.phone-example');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('error.invalid-phone', 'en-us', 'Enter a valid phone number.'),
  ('error.invalid-phone', 'pt-br', 'Informe um número de telefone válido.'),
  ('placeholder.phone-example', 'en-us', 'e.g. (11) 91234-5678'),
  ('placeholder.phone-example', 'pt-br', 'ex.: (11) 91234-5678')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
