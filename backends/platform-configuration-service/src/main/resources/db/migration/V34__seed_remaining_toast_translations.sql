-- Backfill for the toast-coverage audit earlier this session — every one of these success toasts
-- was already firing in the UI, but the translation keys behind them were never seeded, so pt-br
-- users were silently seeing English text. Closing that gap here for all of them at once.

INSERT INTO translation_keys (key) VALUES
  ('title.event-created'),
  ('hint.event-created'),
  ('title.survey-created'),
  ('hint.survey-created'),
  ('title.broadcast-sent'),
  ('hint.broadcast-sent'),
  ('title.request-approved'),
  ('hint.request-approved'),
  ('title.request-rejected'),
  ('hint.request-rejected'),
  ('title.representative-linked'),
  ('title.representative-unlinked'),
  ('title.invite-sent'),
  ('title.invite-resent'),
  ('title.assignment-updated'),
  ('title.link-added'),
  ('hint.link-added'),
  ('title.logo-updated'),
  ('hint.logo-updated');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('title.event-created', 'en-us', 'Event created'),
  ('title.event-created', 'pt-br', 'Evento criado'),
  ('hint.event-created', 'en-us', 'Members will see it on the agenda.'),
  ('hint.event-created', 'pt-br', 'Os membros verão na agenda.'),

  ('title.survey-created', 'en-us', 'Survey created'),
  ('title.survey-created', 'pt-br', 'Enquete criada'),
  ('hint.survey-created', 'en-us', 'Members can vote now.'),
  ('hint.survey-created', 'pt-br', 'Os membros já podem votar.'),

  ('title.broadcast-sent', 'en-us', 'Notification sent'),
  ('title.broadcast-sent', 'pt-br', 'Notificação enviada'),
  ('hint.broadcast-sent', 'en-us', 'Every affiliated member was notified.'),
  ('hint.broadcast-sent', 'pt-br', 'Todos os membros filiados foram notificados.'),

  ('title.request-approved', 'en-us', 'Request approved'),
  ('title.request-approved', 'pt-br', 'Solicitação aprovada'),
  ('hint.request-approved', 'en-us', 'This citizen is now an affiliated member.'),
  ('hint.request-approved', 'pt-br', 'Este cidadão agora é um membro filiado.'),

  ('title.request-rejected', 'en-us', 'Request rejected'),
  ('title.request-rejected', 'pt-br', 'Solicitação rejeitada'),
  ('hint.request-rejected', 'en-us', 'The affiliation request was declined.'),
  ('hint.request-rejected', 'pt-br', 'O pedido de filiação foi recusado.'),

  ('title.representative-linked', 'en-us', 'Politician linked'),
  ('title.representative-linked', 'pt-br', 'Político vinculado'),
  ('title.representative-unlinked', 'en-us', 'Politician unlinked'),
  ('title.representative-unlinked', 'pt-br', 'Político desvinculado'),

  ('title.invite-sent', 'en-us', 'Invite sent'),
  ('title.invite-sent', 'pt-br', 'Convite enviado'),
  ('title.invite-resent', 'en-us', 'Invite resent'),
  ('title.invite-resent', 'pt-br', 'Convite reenviado'),

  ('title.assignment-updated', 'en-us', 'Assignment updated'),
  ('title.assignment-updated', 'pt-br', 'Atribuição atualizada'),

  ('title.link-added', 'en-us', 'Link added'),
  ('title.link-added', 'pt-br', 'Link adicionado'),
  ('hint.link-added', 'en-us', 'Your new social link is now public.'),
  ('hint.link-added', 'pt-br', 'Seu novo link social já está público.'),

  ('title.logo-updated', 'en-us', 'Logo updated'),
  ('title.logo-updated', 'pt-br', 'Logo atualizada'),
  ('hint.logo-updated', 'en-us', 'Your new logo is now public.'),
  ('hint.logo-updated', 'pt-br', 'Sua nova logo já está pública.')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
