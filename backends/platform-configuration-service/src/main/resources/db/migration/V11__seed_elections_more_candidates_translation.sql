-- ============================================================
-- Seeds the "+N more" candidates affordance shown on election cards once a race's roster passes
-- the client-side preview limit (municipal races can have 15+ elected vereadores).
-- ============================================================

INSERT INTO translation_keys (key)
VALUES
  ('label.more-candidates');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('label.more-candidates', 'en-us', 'more'),
  ('label.more-candidates', 'pt-br', 'candidatos')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
