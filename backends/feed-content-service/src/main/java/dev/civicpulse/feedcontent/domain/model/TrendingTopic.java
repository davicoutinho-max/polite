package dev.civicpulse.feedcontent.domain.model;

import java.time.Instant;

/** A single row of the periodically-recomputed {@code trending_topics_cache} — never written to
 * directly by request-handling code, see schema.sql's table comment. */
public record TrendingTopic(String hashtag, long postCountLast24h, short rank, Instant computedAt) {}
