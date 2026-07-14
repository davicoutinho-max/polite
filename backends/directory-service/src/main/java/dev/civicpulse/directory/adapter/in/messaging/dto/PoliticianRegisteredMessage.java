package dev.civicpulse.directory.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of party-management-service's {@code PoliticianRegistered} — see
 * docs/db/party-management-service/schema.sql: {@code PoliticianRegistered(
 * politician_account_id, party_id, cpf_hash, cnpj_hash)}. Directory only needs the party
 * linkage; the hashes are Party Management's own audit concern. */
public record PoliticianRegisteredMessage(UUID politicianAccountId, UUID partyId, String cpfHash, String cnpjHash, Instant occurredAt) {}
