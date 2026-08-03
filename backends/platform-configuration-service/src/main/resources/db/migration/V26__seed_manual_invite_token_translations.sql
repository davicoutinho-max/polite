-- i18n for the manual invite-token entry on the plain registration form — see register-page.ts's
-- redeemInviteToken/toggleTokenField.

INSERT INTO translation_keys (key)
VALUES
  ('button.hide-invite-token'),
  ('button.have-invite-token'),
  ('placeholder.paste-invite-token');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('button.hide-invite-token', 'en-us', 'Hide invite token field'),
  ('button.have-invite-token', 'en-us', 'Are you a party or politician with an invite? Enter your token'),
  ('placeholder.paste-invite-token', 'en-us', 'Paste your invite token…'),
  ('button.hide-invite-token', 'pt-br', 'Ocultar campo de token'),
  ('button.have-invite-token', 'pt-br', 'É um partido ou político com convite? Informe seu token'),
  ('placeholder.paste-invite-token', 'pt-br', 'Cole seu token de convite…')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
