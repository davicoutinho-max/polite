-- The new "show more"/"show less" toggle used to clamp long free-text (post bodies, comments,
-- party history/program, dossier fields like education/profession) — see UiExpandableText.

INSERT INTO translation_keys (key) VALUES ('button.show-more'), ('button.show-less');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('button.show-more', 'en-us', 'Show more'),
  ('button.show-less', 'en-us', 'Show less'),
  ('button.show-more', 'pt-br', 'Ver mais'),
  ('button.show-less', 'pt-br', 'Ver menos')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
