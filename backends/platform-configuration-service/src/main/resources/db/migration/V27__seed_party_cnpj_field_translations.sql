-- The party invite form's CNPJ field — deliberately a new key rather than reusing 'field.cnpj'
-- (already seeded as "CNPJ (gabinete/comitê)" for an unrelated form), since that label would be
-- confusing here: this is the party's own tax id, not a cabinet/committee document.

INSERT INTO translation_keys (key)
VALUES
  ('field.party-cnpj'),
  ('error.invalid-cnpj'),
  ('error.fill-registration-fields-no-document');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('field.party-cnpj', 'en-us', 'CNPJ'),
  ('error.invalid-cnpj', 'en-us', 'Enter a valid CNPJ.'),
  ('error.fill-registration-fields-no-document', 'en-us', 'Fill in your handle, email and password to create an account.'),
  ('field.party-cnpj', 'pt-br', 'CNPJ'),
  ('error.invalid-cnpj', 'pt-br', 'Informe um CNPJ válido.'),
  ('error.fill-registration-fields-no-document', 'pt-br', 'Preencha o identificador, email e senha para criar a conta.')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
