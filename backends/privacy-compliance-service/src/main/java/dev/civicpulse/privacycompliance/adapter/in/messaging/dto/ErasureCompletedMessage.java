package dev.civicpulse.privacycompliance.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Generic shape for the {@code <ServiceName>ErasureCompleted} family documented in schema.sql —
 * every reporting microservice publishes this same record to the shared {@code
 * erasure-completed} topic, distinguished by {@code serviceName} rather than by one topic per
 * service (a single stable contract every future service can report against, instead of a new
 * topic and a new listener method per service). */
public record ErasureCompletedMessage(UUID deletionRequestId, String serviceName, Integer recordCount, Instant occurredAt) {}
