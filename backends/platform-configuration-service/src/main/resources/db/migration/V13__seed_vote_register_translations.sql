-- ============================================================
-- Seeds labels for the new personal/unofficial vote-registration screen (search candidates per
-- office of an election, confirm a personal pick, urna-eletrônica-inspired but distinct UI).
-- ============================================================

INSERT INTO translation_keys (key)
VALUES
  ('button.register-my-vote'),
  ('button.back-to-election'),
  ('button.change'),
  ('button.confirm-vote'),
  ('label.register-my-vote'),
  ('label.vote-disclaimer'),
  ('label.search-candidate'),
  ('label.vote-responsibly'),
  ('label.candidate'),
  ('empty.no-offices.title'),
  ('empty.no-offices-desc'),
  ('empty.sign-in-required.title'),
  ('empty.sign-in-required-vote');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('button.register-my-vote', 'en-us', 'Register my vote'),
  ('button.back-to-election', 'en-us', 'Back to election'),
  ('button.change', 'en-us', 'Change'),
  ('button.confirm-vote', 'en-us', 'CONFIRM'),
  ('label.register-my-vote', 'en-us', 'Register my vote'),
  ('label.vote-disclaimer', 'en-us', 'Personal and unofficial record. Voting in Brazil is secret — this does not replace or represent your official ballot.'),
  ('label.search-candidate', 'en-us', 'Search by name…'),
  ('label.vote-responsibly', 'en-us', 'VOTE WITH RESPONSIBILITY'),
  ('label.candidate', 'en-us', 'Candidate'),
  ('empty.no-offices.title', 'en-us', 'Nothing to register yet'),
  ('empty.no-offices-desc', 'en-us', 'This election has no candidates linked yet.'),
  ('empty.sign-in-required.title', 'en-us', 'Sign in to register your vote'),
  ('empty.sign-in-required-vote', 'en-us', 'Create an account or sign in to keep a personal record of who you voted for.'),

  ('button.register-my-vote', 'pt-br', 'Registrar meu voto'),
  ('button.back-to-election', 'pt-br', 'Voltar para a eleição'),
  ('button.change', 'pt-br', 'Alterar'),
  ('button.confirm-vote', 'pt-br', 'CONFIRMAR'),
  ('label.register-my-vote', 'pt-br', 'Registrar meu voto'),
  ('label.vote-disclaimer', 'pt-br', 'Registro pessoal e não-oficial. O voto no Brasil é secreto — isto não substitui nem representa seu voto oficial na urna.'),
  ('label.search-candidate', 'pt-br', 'Buscar pelo nome…'),
  ('label.vote-responsibly', 'pt-br', 'VOTE COM RESPONSABILIDADE'),
  ('label.candidate', 'pt-br', 'Candidato'),
  ('empty.no-offices.title', 'pt-br', 'Nada para registrar ainda'),
  ('empty.no-offices-desc', 'pt-br', 'Esta eleição ainda não tem candidatos vinculados.'),
  ('empty.sign-in-required.title', 'pt-br', 'Entre para registrar seu voto'),
  ('empty.sign-in-required-vote', 'pt-br', 'Crie uma conta ou entre para manter um registro pessoal de quem você votou.')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
