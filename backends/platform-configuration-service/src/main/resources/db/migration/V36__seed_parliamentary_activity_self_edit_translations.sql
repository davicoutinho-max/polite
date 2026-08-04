-- New self-service editing added to the edit-profile page: mandates, team & advisors, career
-- milestones, and self-reported parliamentary-activity counts (speeches/interviews/trips) — all
-- previously read-only in the frontend despite the backend already exposing add/update endpoints
-- for most of them (see legislative-service's PoliticianDossierController/CareerController).

INSERT INTO translation_keys (key) VALUES
  ('section.parliamentary-activity-counts'),
  ('placeholder.mandate-role'),
  ('placeholder.mandate-period'),
  ('placeholder.team-role'),
  ('placeholder.avatar-url-optional'),
  ('label.name'),
  ('label.year'),
  ('placeholder.milestone-title'),
  ('label.details-optional'),
  ('hint.activity-counts'),
  ('title.mandate-added'),
  ('hint.mandate-added'),
  ('title.team-member-added'),
  ('hint.team-member-added'),
  ('title.milestone-added'),
  ('hint.milestone-added'),
  ('button.edit-profile');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('section.parliamentary-activity-counts', 'en-us', 'Parliamentary activity'),
  ('section.parliamentary-activity-counts', 'pt-br', 'Atividade parlamentar'),

  ('placeholder.mandate-role', 'en-us', 'e.g. Federal Deputy'),
  ('placeholder.mandate-role', 'pt-br', 'ex.: Deputado Federal'),
  ('placeholder.mandate-period', 'en-us', 'e.g. 2023–2027'),
  ('placeholder.mandate-period', 'pt-br', 'ex.: 2023–2027'),

  ('placeholder.team-role', 'en-us', 'e.g. Chief of Staff'),
  ('placeholder.team-role', 'pt-br', 'ex.: Chefe de Gabinete'),
  ('placeholder.avatar-url-optional', 'en-us', 'Photo URL (optional)'),
  ('placeholder.avatar-url-optional', 'pt-br', 'URL da foto (opcional)'),
  ('label.name', 'en-us', 'Name'),
  ('label.name', 'pt-br', 'Nome'),

  ('label.year', 'en-us', 'Year'),
  ('label.year', 'pt-br', 'Ano'),
  ('placeholder.milestone-title', 'en-us', 'e.g. Elected City Councilor'),
  ('placeholder.milestone-title', 'pt-br', 'ex.: Eleito Vereador'),
  ('label.details-optional', 'en-us', 'Details (optional)'),
  ('label.details-optional', 'pt-br', 'Detalhes (opcional)'),

  ('hint.activity-counts', 'en-us', 'Self-reported counts shown on your Parliamentary Activity tab.'),
  ('hint.activity-counts', 'pt-br', 'Números autodeclarados exibidos na sua aba de Atividade Parlamentar.'),

  ('title.mandate-added', 'en-us', 'Mandate added'),
  ('title.mandate-added', 'pt-br', 'Mandato adicionado'),
  ('hint.mandate-added', 'en-us', 'It now shows on your public profile.'),
  ('hint.mandate-added', 'pt-br', 'Ele já aparece no seu perfil público.'),

  ('title.team-member-added', 'en-us', 'Team member added'),
  ('title.team-member-added', 'pt-br', 'Membro da equipe adicionado'),
  ('hint.team-member-added', 'en-us', 'They now show on your public profile.'),
  ('hint.team-member-added', 'pt-br', 'Já aparece no seu perfil público.'),

  ('title.milestone-added', 'en-us', 'Milestone added'),
  ('title.milestone-added', 'pt-br', 'Marco adicionado'),
  ('hint.milestone-added', 'en-us', 'It now shows on your career timeline.'),
  ('hint.milestone-added', 'pt-br', 'Ele já aparece na sua linha do tempo.'),

  ('button.edit-profile', 'en-us', 'Edit profile'),
  ('button.edit-profile', 'pt-br', 'Editar perfil')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
