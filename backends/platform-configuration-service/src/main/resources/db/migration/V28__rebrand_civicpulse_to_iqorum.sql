-- The platform was renamed from CivicPulse to IQORUM — these are the only two seeded translation
-- values that spelled the old brand name out literally (everywhere else it's a hardcoded template
-- string, not a translation).

UPDATE translation_values
SET value = 'The contribution is destined to the party. IQORUM acts only as the payment platform.'
WHERE language_id = 'en-us'
  AND translation_key_id = (SELECT id FROM translation_keys WHERE key = 'hint.contribution-disclaimer');

UPDATE translation_values
SET value = 'A contribuição é destinada ao partido. O IQORUM atua apenas como plataforma de pagamento.'
WHERE language_id = 'pt-br'
  AND translation_key_id = (SELECT id FROM translation_keys WHERE key = 'hint.contribution-disclaimer');

UPDATE translation_values
SET value = 'You''ll complete payment on Asaas''s secure page — IQORUM never sees your card details.'
WHERE language_id = 'en-us'
  AND translation_key_id = (SELECT id FROM translation_keys WHERE key = 'hint.asaas-secure-checkout');

UPDATE translation_values
SET value = 'Você vai concluir o pagamento na página segura do Asaas — o IQORUM nunca vê os dados do seu cartão.'
WHERE language_id = 'pt-br'
  AND translation_key_id = (SELECT id FROM translation_keys WHERE key = 'hint.asaas-secure-checkout');
