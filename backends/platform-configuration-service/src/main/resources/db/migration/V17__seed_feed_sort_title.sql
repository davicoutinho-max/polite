-- ============================================================
-- One more i18n audit gap: FeedSort's title key is passed as a component input default
-- (`titleKey = input('section.recent-activity')`) rather than a literal string in the template's
-- `| translate` expression, so it was missed by the grep-based key extraction used for V15/V16.
-- Confirmed live in the browser: with the locale switched to pt-br, the feed's "Recent Activity"
-- header stayed in English while every other string on the page translated correctly.
-- ============================================================

INSERT INTO translation_keys (key) VALUES ('section.recent-activity');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('section.recent-activity', 'en-us', 'Recent Activity'),
  ('section.recent-activity', 'pt-br', 'Atividade recente')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
