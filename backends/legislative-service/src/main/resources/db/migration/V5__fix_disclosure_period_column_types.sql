-- V4 added period_month/period_year as smallint, but the domain model uses plain Java int
-- (Hibernate schema validation expects integer/int4) — widening the column type to match.

ALTER TABLE accountability_disclosures ALTER COLUMN period_month TYPE integer;
ALTER TABLE accountability_disclosures ALTER COLUMN period_year TYPE integer;
