-- i18n for the party self-service edit form's new fields: name, founded year and presentation
-- video (acronym/number/ideology/president already had keys — see V5).

INSERT INTO translation_keys (key)
VALUES
  ('field.party-name'),
  ('field.founded-year'),
  ('field.video-url'),
  ('section.presentation-video');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('field.party-name', 'en-us', 'Party name'),
  ('field.founded-year', 'en-us', 'Founded year'),
  ('field.video-url', 'en-us', 'Presentation video (YouTube or direct link)'),
  ('section.presentation-video', 'en-us', 'Presentation video'),
  ('field.party-name', 'pt-br', 'Nome do partido'),
  ('field.founded-year', 'pt-br', 'Ano de fundação'),
  ('field.video-url', 'pt-br', 'Vídeo de apresentação (YouTube ou link direto)'),
  ('section.presentation-video', 'pt-br', 'Vídeo de apresentação')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
