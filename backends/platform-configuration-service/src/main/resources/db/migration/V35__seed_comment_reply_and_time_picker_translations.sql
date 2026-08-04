-- Two more gaps from earlier this session: the comment-reply feature's "Reply" button and its
-- inline reply input, and the agenda time-picker's "OK" confirm button. aria.cancel-reply already
-- existed (from the messages feature) and is reused as-is here.

INSERT INTO translation_keys (key) VALUES
  ('button.reply'),
  ('label.write-a-reply'),
  ('aria.write-a-reply'),
  ('action.ok');

INSERT INTO translation_values (translation_key_id, language_id, value)
SELECT tk.id, v.language_id, v.value
FROM (
VALUES
  ('button.reply', 'en-us', 'Reply'),
  ('button.reply', 'pt-br', 'Responder'),
  ('label.write-a-reply', 'en-us', 'Write a reply…'),
  ('label.write-a-reply', 'pt-br', 'Escreva uma resposta…'),
  ('aria.write-a-reply', 'en-us', 'Write a reply'),
  ('aria.write-a-reply', 'pt-br', 'Escrever uma resposta'),
  ('action.ok', 'en-us', 'OK'),
  ('action.ok', 'pt-br', 'OK')
) AS v(key, language_id, value)
JOIN translation_keys tk ON tk.key = v.key;
