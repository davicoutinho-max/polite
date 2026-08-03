-- CivicPulse is a Brazilian civic-transparency platform — the default language for anyone who
-- hasn't explicitly picked one should be pt-br, not en-us (V4's original seed picked English
-- arbitrarily; both languages are fully translated either way, see LocaleService).

UPDATE languages SET is_default = false WHERE id = 'en-us';
UPDATE languages SET is_default = true WHERE id = 'pt-br';
