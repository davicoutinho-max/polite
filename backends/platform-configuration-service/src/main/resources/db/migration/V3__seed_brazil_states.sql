-- ============================================================
-- Seeds Brazil and its 27 states/federal district — the countries/states registry had never
-- been seeded with real data, only leftover test debris from repeated integration test runs
-- (a since-fixed test cleanup bug). Location dropdowns across the app (agenda location,
-- politician registration) read from this table, so it must carry real options.
-- ============================================================

DELETE FROM states WHERE country_id IN (SELECT id FROM countries WHERE name LIKE 'Test Country%' OR name = 'Verification Land');
DELETE FROM countries WHERE name LIKE 'Test Country%' OR name = 'Verification Land';

INSERT INTO countries (id, name, code)
VALUES (gen_random_uuid(), 'Brazil', 'BR')
ON CONFLICT (code) DO NOTHING;

INSERT INTO states (country_id, name, code)
SELECT c.id, s.name, s.code
FROM countries c
CROSS JOIN (VALUES
  ('Acre', 'AC'),
  ('Alagoas', 'AL'),
  ('Amapá', 'AP'),
  ('Amazonas', 'AM'),
  ('Bahia', 'BA'),
  ('Ceará', 'CE'),
  ('Distrito Federal', 'DF'),
  ('Espírito Santo', 'ES'),
  ('Goiás', 'GO'),
  ('Maranhão', 'MA'),
  ('Mato Grosso', 'MT'),
  ('Mato Grosso do Sul', 'MS'),
  ('Minas Gerais', 'MG'),
  ('Pará', 'PA'),
  ('Paraíba', 'PB'),
  ('Paraná', 'PR'),
  ('Pernambuco', 'PE'),
  ('Piauí', 'PI'),
  ('Rio de Janeiro', 'RJ'),
  ('Rio Grande do Norte', 'RN'),
  ('Rio Grande do Sul', 'RS'),
  ('Rondônia', 'RO'),
  ('Roraima', 'RR'),
  ('Santa Catarina', 'SC'),
  ('São Paulo', 'SP'),
  ('Sergipe', 'SE'),
  ('Tocantins', 'TO')
) AS s(name, code)
WHERE c.code = 'BR';
