-- The disclosure form only ever let a politician declare an amount and attach one document — no
-- way to add context (why the expense was necessary, what it covers, etc.). Optional free-text
-- field so a submission can carry more than just a number and a receipt.

ALTER TABLE accountability_disclosures ADD COLUMN notes text;
