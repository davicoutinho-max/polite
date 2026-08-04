-- Accountability disclosure submissions could only ever carry a declared amount and a receipt —
-- no way to add context. Added an optional free-text notes field (legislative-service V3
-- migration) plus a real success/rejection toast for the submission itself, which previously gave
-- no feedback at all.

INSERT INTO translation_keys (key) VALUES
  ('label.disclosure-notes'),
  ('placeholder.disclosure-notes'),
  ('title.disclosure-approved'),
  ('hint.disclosure-approved'),
  ('title.disclosure-rejected');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('label.disclosure-notes', 'en-us', 'Additional notes (optional)'),
  ('label.disclosure-notes', 'pt-br', 'Observações adicionais (opcional)'),
  ('placeholder.disclosure-notes', 'en-us', 'Explain what this expense covers, why it was necessary, etc.'),
  ('placeholder.disclosure-notes', 'pt-br', 'Explique o que essa despesa cobre, por que foi necessária, etc.'),
  ('title.disclosure-approved', 'en-us', 'Disclosure approved'),
  ('title.disclosure-approved', 'pt-br', 'Prestação de contas aprovada'),
  ('hint.disclosure-approved', 'en-us', 'The AI reviewer confirmed your document matches the declared amount.'),
  ('hint.disclosure-approved', 'pt-br', 'O revisor de IA confirmou que o documento corresponde ao valor declarado.'),
  ('title.disclosure-rejected', 'en-us', 'Disclosure rejected'),
  ('title.disclosure-rejected', 'pt-br', 'Prestação de contas rejeitada')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
