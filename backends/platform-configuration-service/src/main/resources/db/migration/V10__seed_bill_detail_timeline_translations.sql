-- ============================================================
-- Seeds translation keys for the Bills detail page's new "Processing timeline" section (the
-- tramitação history already shown elsewhere via bill-card's "View history" dialog, now also
-- rendered inline on the dedicated bill detail page).
-- ============================================================

INSERT INTO translation_keys (key)
VALUES
  ('label.processing-timeline'),
  ('empty.no-history.title'),
  ('empty.no-history-for-bill');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('label.processing-timeline', 'en-us', 'Processing timeline'),
  ('empty.no-history.title', 'en-us', 'No processing history'),
  ('empty.no-history-for-bill', 'en-us', 'No tramitação history is available for this bill in the official open-data service.'),
  ('label.processing-timeline', 'pt-br', 'Linha do tempo de tramitação'),
  ('empty.no-history.title', 'pt-br', 'Sem histórico de tramitação'),
  ('empty.no-history-for-bill', 'pt-br', 'Não há histórico de tramitação disponível para este projeto no serviço de dados abertos oficial.')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
