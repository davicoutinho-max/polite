-- ============================================================
-- Closes the last gap found by a second, deeper i18n audit pass: V6 missed every key built
-- dynamically at runtime rather than appearing as a string literal next to translate: —
-- NavItem's key field (consumed via a dynamic pipe binding in ui-nav-item.ts) and the
-- status-prefixed template-literal keys built in admin-page.ts/wallet-page.ts. Everything else
-- that pattern could hide (category, spectrum, status pending/paid/overdue, every ternary-
-- selected button/label key) turned out to already be covered by V5/V6.
-- ============================================================

INSERT INTO translation_keys (key)
VALUES
  ('nav.bills'),
  ('status.approved'),
  ('status.rejected')
ON CONFLICT (key) DO NOTHING;

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('nav.bills', 'en-us', 'Bills'),
  ('status.approved', 'en-us', 'Approved'),
  ('status.rejected', 'en-us', 'Rejected'),
  ('nav.bills', 'pt-br', 'Projetos de Lei'),
  ('status.approved', 'pt-br', 'Aprovado'),
  ('status.rejected', 'pt-br', 'Rejeitado')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key
ON CONFLICT (translation_key_id, language_id) DO NOTHING;
