package dev.civicpulse.legislative.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of party-management-service's {@code PoliticianRegistered} — this service only
 * needs the account id to lazily create a dossier-extension stub (see schema.sql's
 * politician_dossier_extensions comment). */
public record PoliticianRegisteredMessage(UUID politicianAccountId, UUID partyId, String cpfHash, String cnpjHash, Instant occurredAt) {}
