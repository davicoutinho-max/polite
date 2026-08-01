-- i18n for the new "generate social media versions" feature in the post composer (AI-generated
-- Instagram/Facebook/X/LinkedIn variants + a plain-language summary).

INSERT INTO translation_keys (key)
VALUES
  ('aria.generate-social-variants'),
  ('label.social-media-versions'),
  ('hint.generating-variants'),
  ('error.generate-variants-failed'),
  ('tab.variant-instagram'),
  ('tab.variant-facebook'),
  ('tab.variant-x'),
  ('tab.variant-linkedin'),
  ('tab.variant-simple-summary'),
  ('button.copy'),
  ('hint.social-variants-manual'),
  ('title.copied-to-clipboard'),
  ('hint.copied-to-clipboard');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('aria.generate-social-variants', 'en-us', 'Generate social media versions'),
  ('label.social-media-versions', 'en-us', 'Social media versions'),
  ('hint.generating-variants', 'en-us', 'The AI is writing versions for each network…'),
  ('error.generate-variants-failed', 'en-us', 'Could not generate social media versions — please try again.'),
  ('tab.variant-instagram', 'en-us', 'Instagram'),
  ('tab.variant-facebook', 'en-us', 'Facebook'),
  ('tab.variant-x', 'en-us', 'X'),
  ('tab.variant-linkedin', 'en-us', 'LinkedIn'),
  ('tab.variant-simple-summary', 'en-us', 'Simple summary'),
  ('button.copy', 'en-us', 'Copy'),
  ('hint.social-variants-manual', 'en-us', 'Review and edit as needed, then paste it into each network yourself for now.'),
  ('title.copied-to-clipboard', 'en-us', 'Copied to clipboard'),
  ('hint.copied-to-clipboard', 'en-us', 'Paste it directly into the app of your choice.'),

  ('aria.generate-social-variants', 'pt-br', 'Gerar versões para redes sociais'),
  ('label.social-media-versions', 'pt-br', 'Versões para redes sociais'),
  ('hint.generating-variants', 'pt-br', 'A IA está escrevendo versões para cada rede…'),
  ('error.generate-variants-failed', 'pt-br', 'Não foi possível gerar as versões para redes sociais — tente novamente.'),
  ('tab.variant-instagram', 'pt-br', 'Instagram'),
  ('tab.variant-facebook', 'pt-br', 'Facebook'),
  ('tab.variant-x', 'pt-br', 'X'),
  ('tab.variant-linkedin', 'pt-br', 'LinkedIn'),
  ('tab.variant-simple-summary', 'pt-br', 'Resumo simples'),
  ('button.copy', 'pt-br', 'Copiar'),
  ('hint.social-variants-manual', 'pt-br', 'Revise e edite se precisar, depois cole em cada rede você mesmo por enquanto.'),
  ('title.copied-to-clipboard', 'pt-br', 'Copiado para a área de transferência'),
  ('hint.copied-to-clipboard', 'pt-br', 'Cole diretamente no aplicativo de sua escolha.')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
