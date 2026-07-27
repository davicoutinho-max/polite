-- ============================================================
-- Seeds translation keys for the Bills page's type filter (added alongside a detail-view refactor
-- of that page) — the filter's "All types" option and the five LEGISLATIVE_BILL_TYPES section
-- labels (PL/PEC/PLP/MPV/PDL) were plain hardcoded English strings with no TranslateService key at
-- all, so they rendered untranslated even on an otherwise fully Portuguese page.
-- ============================================================

INSERT INTO translation_keys (key)
VALUES
  ('label.all-bill-types'),
  ('bill-type.PL'),
  ('bill-type.PEC'),
  ('bill-type.PLP'),
  ('bill-type.MPV'),
  ('bill-type.PDL');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('label.all-bill-types', 'en-us', 'All types'),
  ('bill-type.PL', 'en-us', 'Bill (PL)'),
  ('bill-type.PEC', 'en-us', 'Constitutional Amendment (PEC)'),
  ('bill-type.PLP', 'en-us', 'Complementary Law (PLP)'),
  ('bill-type.MPV', 'en-us', 'Provisional Measure (MPV)'),
  ('bill-type.PDL', 'en-us', 'Legislative Decree (PDL)'),
  ('label.all-bill-types', 'pt-br', 'Todos os tipos'),
  ('bill-type.PL', 'pt-br', 'Projeto de Lei (PL)'),
  ('bill-type.PEC', 'pt-br', 'Emenda Constitucional (PEC)'),
  ('bill-type.PLP', 'pt-br', 'Lei Complementar (PLP)'),
  ('bill-type.MPV', 'pt-br', 'Medida Provisória (MPV)'),
  ('bill-type.PDL', 'pt-br', 'Decreto Legislativo (PDL)')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
