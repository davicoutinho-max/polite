-- i18n for the invite-token registration flow (party/politician invites replacing the old
-- "admin/party sets the password directly" flows) — see RegistrationToken's javadoc.

INSERT INTO translation_keys (key)
VALUES
  ('title.register-party'),
  ('title.register-politician'),
  ('hint.invite-registered-as'),
  ('hint.checking-invite'),
  ('field.invite-email'),
  ('button.send-invite'),
  ('section.invite-party'),
  ('section.party-invites'),
  ('section.invite-politician'),
  ('section.politician-invites'),
  ('label.expired'),
  ('button.resend'),
  ('empty.no-party-invites.title'),
  ('empty.no-politician-invites.title'),
  ('button.invite-party'),
  ('hint.invite-party'),
  ('error.invalid-invite');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('title.register-party', 'en-us', 'Register your party'),
  ('title.register-politician', 'en-us', 'Register as a politician'),
  ('hint.invite-registered-as', 'en-us', 'You''ve been invited to register'),
  ('hint.checking-invite', 'en-us', 'Checking your invite…'),
  ('field.invite-email', 'en-us', 'Contact email'),
  ('button.send-invite', 'en-us', 'Send invite'),
  ('section.invite-party', 'en-us', 'Invite a party'),
  ('section.party-invites', 'en-us', 'Party invites'),
  ('section.invite-politician', 'en-us', 'Invite a politician'),
  ('section.politician-invites', 'en-us', 'Politician invites'),
  ('label.expired', 'en-us', 'Expired'),
  ('button.resend', 'en-us', 'Resend'),
  ('empty.no-party-invites.title', 'en-us', 'No invites sent yet'),
  ('empty.no-politician-invites.title', 'en-us', 'No invites sent yet'),
  ('button.invite-party', 'en-us', 'Invite party'),
  ('hint.invite-party', 'en-us', 'These fields become the party''s official identity — the invite is emailed to their contact, who picks their own password when redeeming it.'),
  ('error.invalid-invite', 'en-us', 'This invite link is invalid or has expired.'),
  ('title.register-party', 'pt-br', 'Registre seu partido'),
  ('title.register-politician', 'pt-br', 'Registre-se como político'),
  ('hint.invite-registered-as', 'pt-br', 'Você foi convidado a se registrar'),
  ('hint.checking-invite', 'pt-br', 'Verificando seu convite…'),
  ('field.invite-email', 'pt-br', 'Email de contato'),
  ('button.send-invite', 'pt-br', 'Enviar convite'),
  ('section.invite-party', 'pt-br', 'Convidar um partido'),
  ('section.party-invites', 'pt-br', 'Convites de partidos'),
  ('section.invite-politician', 'pt-br', 'Convidar um político'),
  ('section.politician-invites', 'pt-br', 'Convites de políticos'),
  ('label.expired', 'pt-br', 'Expirado'),
  ('button.resend', 'pt-br', 'Reenviar'),
  ('empty.no-party-invites.title', 'pt-br', 'Nenhum convite enviado ainda'),
  ('empty.no-politician-invites.title', 'pt-br', 'Nenhum convite enviado ainda'),
  ('button.invite-party', 'pt-br', 'Convidar partido'),
  ('hint.invite-party', 'pt-br', 'Esses campos se tornam a identidade oficial do partido — o convite é enviado por email ao contato informado, que escolhe sua própria senha ao resgatá-lo.'),
  ('error.invalid-invite', 'pt-br', 'Este link de convite é inválido ou expirou.')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
