-- Seed data mirroring the frontend mock's original 3 canned topics — static reference content,
-- not user-generated, so it ships as a migration rather than an application-layer bootstrap.
INSERT INTO assistant_topics (id, reference, title) VALUES
  ('11111111-1111-1111-1111-111111111111', 'PEC 33/2024', 'Fiscal Transparency Amendment'),
  ('22222222-2222-2222-2222-222222222222', 'H.R. 452', 'Clean Water Infrastructure Act'),
  ('33333333-3333-3333-3333-333333333333', 'CPI 05/2023', 'Public Contracts Inquiry');

INSERT INTO assistant_answers (topic_id, prompt_kind, answer_text) VALUES
  ('11111111-1111-1111-1111-111111111111', 'summary',
   'PEC 33/2024 amends the Constitution to require every level of government to publish budget execution data in an open, machine-readable format within 30 days of each transaction, and creates an independent oversight council to audit compliance.'),
  ('11111111-1111-1111-1111-111111111111', 'plain',
   'Think of it as a rule that forces governments to show, almost in real time, where public money goes — in a format anyone can download and check. It also sets up a watchdog group to make sure they actually do it.'),
  ('11111111-1111-1111-1111-111111111111', 'impact',
   'If approved: federal, state and municipal bodies would have to open their spending data; citizens and journalists could track contracts faster; and non-compliant officials could face sanctions from the new council. It would take effect 180 days after promulgation.'),
  ('22222222-2222-2222-2222-222222222222', 'summary',
   'H.R. 452 funds the modernization of municipal water-treatment plants and the replacement of lead service pipes across urban districts over a ten-year horizon, with matching grants for smaller cities.'),
  ('22222222-2222-2222-2222-222222222222', 'plain',
   'It is a plan to fix old water systems and remove lead pipes so tap water is safer, especially in cities. Bigger cities help pay; smaller ones get extra federal help.'),
  ('22222222-2222-2222-2222-222222222222', 'impact',
   'If approved: cities could apply for grants starting next fiscal year, lead-pipe replacement would be prioritized in high-risk zones, and water utilities would report progress annually. Households in affected areas should see reduced contamination risk within 3-5 years.'),
  ('33333333-3333-3333-3333-333333333333', 'summary',
   'CPI 05/2023 is a parliamentary commission of inquiry investigating irregularities in public procurement contracts signed between 2020 and 2022, with power to summon witnesses and request documents.'),
  ('33333333-3333-3333-3333-333333333333', 'plain',
   'It is a formal investigation by lawmakers into whether some government contracts were awarded unfairly. They can call people to testify and demand paperwork.'),
  ('33333333-3333-3333-3333-333333333333', 'impact',
   'If its findings are approved: the commission may refer cases to prosecutors, recommend new procurement rules, and publish a public report. It does not itself convict anyone — it investigates and recommends.');
