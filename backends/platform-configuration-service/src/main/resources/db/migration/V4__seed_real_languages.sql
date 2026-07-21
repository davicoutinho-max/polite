-- ============================================================
-- The languages table only ever held leftover test debris ("Test Language A/B", one of them
-- incorrectly flagged as the platform default) from repeated integration test runs (a since-fixed
-- test cleanup bug) — no real language was ever seeded, so LocaleService's hardcoded 'en-us'
-- default never matched anything and silently fell back to whatever row came first.
-- ============================================================

DELETE FROM translation_values WHERE language_id LIKE 'test-lang-%';
DELETE FROM languages WHERE id LIKE 'test-lang-%';

INSERT INTO languages (id, name, code, is_default)
VALUES ('en-us', 'English', 'en', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO languages (id, name, code, is_default)
VALUES ('pt-br', 'Português', 'pt', false)
ON CONFLICT (id) DO NOTHING;
