-- Missed in V24 — the "Invite a politician" form's hint text.

INSERT INTO translation_keys (key) VALUES ('hint.invite-politician');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('hint.invite-politician', 'en-us', 'Only parties can invite a politician. They''ll receive an email to complete their own registration and pick their own password.'),
  ('hint.invite-politician', 'pt-br', 'Somente partidos podem convidar um político. Ele receberá um email para completar seu próprio cadastro e escolher sua própria senha.')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
