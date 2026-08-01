-- i18n for real cross-posting to Facebook/Instagram/X: the social-connections settings page
-- (connect/disconnect, OAuth redirect-back toasts) and the "also publish to" chips in the post
-- composer plus the resulting share-result toasts.

INSERT INTO translation_keys (key)
VALUES
  ('account.social-networks'),
  ('page.social-connections.title'),
  ('page.social-connections.subtitle'),
  ('section.meta-connection'),
  ('section.x-connection'),
  ('hint.meta-connection'),
  ('hint.x-connection'),
  ('button.connect-meta'),
  ('button.connect-x'),
  ('button.disconnect'),
  ('label.not-connected'),
  ('hint.loading'),
  ('title.social-connected'),
  ('title.social-connect-failed'),
  ('hint.social-connect-failed'),
  ('error.disconnect-failed'),
  ('label.publish-to-networks'),
  ('title.social-share-success'),
  ('title.social-share-partial'),
  ('title.social-share-failed'),
  ('hint.social-share-failed');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('account.social-networks', 'en-us', 'Social networks'),
  ('page.social-connections.title', 'en-us', 'Social Networks'),
  ('page.social-connections.subtitle', 'en-us', 'Connect Facebook, Instagram and X to publish your posts there automatically.'),
  ('section.meta-connection', 'en-us', 'Facebook & Instagram'),
  ('section.x-connection', 'en-us', 'X (Twitter)'),
  ('hint.meta-connection', 'en-us', 'One login connects both your Facebook Page and its linked Instagram Business account.'),
  ('hint.x-connection', 'en-us', 'Posting via X''s API requires paid access on our app — connecting is still useful once that''s active.'),
  ('button.connect-meta', 'en-us', 'Connect with Facebook'),
  ('button.connect-x', 'en-us', 'Connect with X'),
  ('button.disconnect', 'en-us', 'Disconnect'),
  ('label.not-connected', 'en-us', 'Not connected'),
  ('hint.loading', 'en-us', 'Loading…'),
  ('title.social-connected', 'en-us', 'Connected!'),
  ('title.social-connect-failed', 'en-us', 'Connection failed'),
  ('hint.social-connect-failed', 'en-us', 'Could not connect — please try again.'),
  ('error.disconnect-failed', 'en-us', 'Could not disconnect — please try again.'),
  ('label.publish-to-networks', 'en-us', 'Also publish to'),
  ('title.social-share-success', 'en-us', 'Shared!'),
  ('title.social-share-partial', 'en-us', 'Some networks failed'),
  ('title.social-share-failed', 'en-us', 'Sharing failed'),
  ('hint.social-share-failed', 'en-us', 'Could not share this post to the selected networks.'),

  ('account.social-networks', 'pt-br', 'Redes sociais'),
  ('page.social-connections.title', 'pt-br', 'Redes Sociais'),
  ('page.social-connections.subtitle', 'pt-br', 'Conecte Facebook, Instagram e X para publicar suas postagens lá automaticamente.'),
  ('section.meta-connection', 'pt-br', 'Facebook e Instagram'),
  ('section.x-connection', 'pt-br', 'X (Twitter)'),
  ('hint.meta-connection', 'pt-br', 'Um único login conecta sua Página do Facebook e a conta do Instagram vinculada a ela.'),
  ('hint.x-connection', 'pt-br', 'Publicar pela API do X exige acesso pago em nosso aplicativo — conectar já é útil assim que isso estiver ativo.'),
  ('button.connect-meta', 'pt-br', 'Conectar com o Facebook'),
  ('button.connect-x', 'pt-br', 'Conectar com o X'),
  ('button.disconnect', 'pt-br', 'Desconectar'),
  ('label.not-connected', 'pt-br', 'Não conectado'),
  ('hint.loading', 'pt-br', 'Carregando…'),
  ('title.social-connected', 'pt-br', 'Conectado!'),
  ('title.social-connect-failed', 'pt-br', 'Falha na conexão'),
  ('hint.social-connect-failed', 'pt-br', 'Não foi possível conectar — tente novamente.'),
  ('error.disconnect-failed', 'pt-br', 'Não foi possível desconectar — tente novamente.'),
  ('label.publish-to-networks', 'pt-br', 'Publicar também em'),
  ('title.social-share-success', 'pt-br', 'Compartilhado!'),
  ('title.social-share-partial', 'pt-br', 'Algumas redes falharam'),
  ('title.social-share-failed', 'pt-br', 'Falha ao compartilhar'),
  ('hint.social-share-failed', 'pt-br', 'Não foi possível compartilhar esta postagem nas redes selecionadas.')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
