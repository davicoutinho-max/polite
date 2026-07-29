-- ============================================================
-- Seeds labels for: (1) the Bills page's new party filter/sort controls, and (2) the "Ask AI"
-- widget's quick-prompt buttons and disclaimer now that it calls a real, live model.
-- ============================================================

INSERT INTO translation_keys (key)
VALUES
  ('label.most-recent'),
  ('label.party-az'),
  ('button.ask-ai-summarize'),
  ('button.ask-ai-explain-simply'),
  ('button.ask-ai-impact'),
  ('hint.ask-ai-disclaimer-live'),
  ('hint.ask-ai-unavailable');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('label.most-recent', 'en-us', 'Most recent'),
  ('label.party-az', 'en-us', 'Party (A–Z)'),
  ('button.ask-ai-summarize', 'en-us', 'Summarize'),
  ('button.ask-ai-explain-simply', 'en-us', 'Explain simply'),
  ('button.ask-ai-impact', 'en-us', 'What changes if approved?'),
  ('hint.ask-ai-disclaimer-live', 'en-us', 'Answered by an AI model scoped to this bill only. It can be incomplete and is not legal advice — always check the official page.'),
  ('hint.ask-ai-unavailable', 'en-us', 'The AI assistant is temporarily unavailable — please try again shortly.'),

  ('label.most-recent', 'pt-br', 'Mais recentes'),
  ('label.party-az', 'pt-br', 'Partido (A–Z)'),
  ('button.ask-ai-summarize', 'pt-br', 'Resumir'),
  ('button.ask-ai-explain-simply', 'pt-br', 'Explicar de forma simples'),
  ('button.ask-ai-impact', 'pt-br', 'O que muda se for aprovado?'),
  ('hint.ask-ai-disclaimer-live', 'pt-br', 'Respondido por um modelo de IA restrito a este projeto de lei. Pode ser incompleto e não substitui aconselhamento jurídico — sempre confira a página oficial.'),
  ('hint.ask-ai-unavailable', 'pt-br', 'O assistente de IA está temporariamente indisponível — tente novamente em instantes.')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
