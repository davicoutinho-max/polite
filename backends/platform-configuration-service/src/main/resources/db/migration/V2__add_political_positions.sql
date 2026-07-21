-- ============================================================
-- Political positions (cargos) — parametrized so the full range of elected offices in
-- Brazilian politics is available when registering a politician, editable here rather than
-- hardcoded in application code.
-- ============================================================

CREATE TABLE political_positions (
  id                           uuid DEFAULT gen_random_uuid(),
  name                         text NOT NULL UNIQUE,
  sort_order                   smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

INSERT INTO political_positions (name, sort_order) VALUES
  ('Vereador', 1),
  ('Prefeito', 2),
  ('Vice-Prefeito', 3),
  ('Deputado Estadual', 4),
  ('Deputado Distrital', 5),
  ('Deputado Federal', 6),
  ('Senador', 7),
  ('Governador', 8),
  ('Vice-Governador', 9),
  ('Presidente da República', 10),
  ('Vice-Presidente da República', 11);
