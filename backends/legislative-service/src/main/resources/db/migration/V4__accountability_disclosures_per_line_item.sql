-- Accountability disclosures used to attach to 5 generic categories (office budget, CEAP quota,
-- amendments, travel, advertising) — the transparency tab actually shows the real compensation/
-- CEAP/office-budget line items (30 of them), so each of THOSE now needs its own accountability
-- category, and each submission needs to say which month/year it covers (public-money reporting
-- is inherently periodic, not a one-off declaration). The 5 old category codes are left in place
-- (existing rows may still reference them) but are no longer offered by the frontend.

ALTER TABLE accountability_disclosures ADD COLUMN period_month smallint;
ALTER TABLE accountability_disclosures ADD COLUMN period_year smallint;
UPDATE accountability_disclosures
SET period_month = EXTRACT(MONTH FROM submitted_at), period_year = EXTRACT(YEAR FROM submitted_at)
WHERE period_month IS NULL;
ALTER TABLE accountability_disclosures ALTER COLUMN period_month SET NOT NULL;
ALTER TABLE accountability_disclosures ALTER COLUMN period_year SET NOT NULL;
ALTER TABLE accountability_disclosures ADD CONSTRAINT chk_period_month CHECK (period_month BETWEEN 1 AND 12);

-- PARAMETER TABLE — add a row to introduce a new value, never a migration.
INSERT INTO accountability_category_options (code, label, sort_order) VALUES
  ('comp_subsidy', 'Subsídio (salário)', 10),
  ('comp_13th_salary', '13º salário', 11),
  ('comp_housing_allowance', 'Auxílio-moradia (ou apartamento funcional)', 12),
  ('comp_ceap', 'Cota para o Exercício da Atividade Parlamentar (CEAP)', 13),
  ('comp_office_budget', 'Verba de Gabinete', 14),
  ('comp_per_diem', 'Diárias (em missões oficiais)', 15),
  ('comp_official_flights', 'Passagens aéreas oficiais', 16),
  ('comp_medical_care', 'Atendimento médico pelo Departamento Médico da Câmara', 17),
  ('comp_health_plan', 'Plano de saúde (conforme regras da Câmara)', 18),
  ('comp_pension', 'Previdência parlamentar (opcional)', 19),
  ('ceap_fuel', 'Combustível', 20),
  ('ceap_flights', 'Passagens aéreas', 21),
  ('ceap_lodging', 'Hospedagem', 22),
  ('ceap_office_rent', 'Aluguel de escritório', 23),
  ('ceap_telephony', 'Telefonia', 24),
  ('ceap_internet', 'Internet', 25),
  ('ceap_postal', 'Correios', 26),
  ('ceap_office_supplies', 'Material de escritório', 27),
  ('ceap_vehicle_rental', 'Locação de veículos', 28),
  ('ceap_vehicle_maintenance', 'Manutenção de veículos', 29),
  ('ceap_taxi_transport', 'Táxi e transporte', 30),
  ('ceap_consulting', 'Consultorias', 31),
  ('ceap_outreach', 'Divulgação da atividade parlamentar', 32),
  ('ceap_content_production', 'Produção de conteúdo', 33),
  ('ceap_subscriptions', 'Assinaturas de jornais e revistas', 34),
  ('ceap_graphic_services', 'Serviços gráficos', 35),
  ('ceap_equipment', 'Equipamentos para o exercício do mandato', 36),
  ('gabinete_aides', 'Contratação de assessores parlamentares', 37),
  ('gabinete_chief_of_staff', 'Chefia de gabinete', 38),
  ('gabinete_secretaries', 'Secretários parlamentares', 39);
