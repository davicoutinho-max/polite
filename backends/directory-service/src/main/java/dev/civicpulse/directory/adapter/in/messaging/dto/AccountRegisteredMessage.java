package dev.civicpulse.directory.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of identity-service's {@code AccountRegistered} — see
 * docs/db/identity-service/schema.sql: {@code AccountRegistered(account_id, account_type,
 * document_hash)}. Directory only needs accountId/accountType; the rest is enriched via
 * {@link dev.civicpulse.directory.application.port.out.AccountLookupGateway}. */
public record AccountRegisteredMessage(UUID accountId, String accountType, String documentHash, Instant occurredAt) {}
