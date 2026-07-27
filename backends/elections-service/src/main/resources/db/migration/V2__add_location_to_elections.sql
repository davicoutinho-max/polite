-- Government-data sync (see government-sync-service's TSE integration) needs to tell apart
-- same-day, same-scope elections held in different places — e.g. every state's 2022 "Eleições
-- Estaduais" shares the same (scope, election_date), and every município's 2024 "Eleições
-- Municipais" too. Nullable and free-text on purpose: NACIONAL elections have no location at all,
-- and ESTADUAL/MUNICIPAL reuse the exact same UF-or-município-name convention already established
-- for directory-service's Politician.state column (UF for state-level, município name for
-- municipal-level) rather than inventing a new one.
ALTER TABLE elections ADD COLUMN location text;
