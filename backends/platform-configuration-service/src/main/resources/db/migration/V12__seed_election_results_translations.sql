-- ============================================================
-- Seeds labels for the election detail page's new ranked-results section (real TSE vote counts
-- per office, winner/2nd/3rd and everyone else, replacing the old plain "linked candidates" grid
-- whenever real results are available for that election).
-- ============================================================

INSERT INTO translation_keys (key)
VALUES
  ('label.results'),
  ('label.elected'),
  ('label.show-less'),
  ('label.show-all-candidates');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('label.results', 'en-us', 'Results'),
  ('label.elected', 'en-us', 'Elected'),
  ('label.show-less', 'en-us', 'Show less'),
  ('label.show-all-candidates', 'en-us', 'Show all'),
  ('label.results', 'pt-br', 'Resultados'),
  ('label.elected', 'pt-br', 'Eleito'),
  ('label.show-less', 'pt-br', 'Mostrar menos'),
  ('label.show-all-candidates', 'pt-br', 'Mostrar todos')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
