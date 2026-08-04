-- Accountability disclosures were redesigned to attach directly to each compensation/CEAP/
-- office-budget line item (with a month/year period and a public document link) instead of a
-- generic 5-category list — new strings for that per-item upload UI.

INSERT INTO translation_keys (key) VALUES
  ('label.period'),
  ('aria.submit-disclosure'),
  ('button.view-document'),
  ('hint.accountability-intro');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('label.period', 'en-us', 'Month / Year'),
  ('label.period', 'pt-br', 'Mês / Ano'),
  ('aria.submit-disclosure', 'en-us', 'Submit accountability report'),
  ('aria.submit-disclosure', 'pt-br', 'Enviar prestação de contas'),
  ('button.view-document', 'en-us', 'View document'),
  ('button.view-document', 'pt-br', 'Ver documento'),
  (
    'hint.accountability-intro',
    'en-us',
    'Each item below is one real type of public money this politician is accountable for. Click the upload icon to file a monthly accountability report — an AI reviewer reads the attached proof and checks it against the declared amount. Anyone can open the attached document.'
  ),
  (
    'hint.accountability-intro',
    'pt-br',
    'Cada item abaixo é um tipo real de dinheiro público pelo qual este político é responsável. Clique no ícone de upload para enviar uma prestação de contas mensal — um revisor de IA lê o comprovante anexado e verifica se ele confere com o valor declarado. Qualquer pessoa pode abrir o documento anexado.'
  )
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
