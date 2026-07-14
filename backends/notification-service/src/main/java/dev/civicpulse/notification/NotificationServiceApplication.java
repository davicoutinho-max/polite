package dev.civicpulse.notification;

import dev.civicpulse.notification.adapter.out.client.FundraisingServiceProperties;
import dev.civicpulse.notification.adapter.out.client.MembershipAffiliationServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Scope note: schema.sql's consumed-events list is deliberately open-ended ("...every other
 * note-worthy event in the system"), since this service is the system's single fan-in point for
 * alerts. This pass wires four concrete, genuinely resolvable consumers: {@code
 * AffiliationConfirmed} and {@code ContributionReceived} carry their recipient's account id
 * directly; {@code MembershipFeeGenerated} and {@code FundraiserGoalReached} don't, so each is
 * paired with a real synchronous lookup back to the producing service (see
 * AffiliationLookupGateway / FundraiserLookupGateway) — the same anti-corruption-layer pattern
 * party-management-service uses against identity-service.
 *
 * <p>Deliberately NOT wired in this pass: {@code PostPublished} ("fanned out to followers"),
 * {@code PartyProfileUpdated}, and any other follower-fan-out event. Fanning out requires
 * resolving "who follows this author," and directory-service (which owns the follow relationship)
 * exposes only {@code POST}/{@code DELETE /follows} today, no query endpoint to list an entity's
 * followers. Inventing that endpoint would mean reopening an already-built, tested service purely
 * to serve this one; the ingestion pipeline here (the actually-hard, schema-mandated part —
 * partitioned storage, idempotent dedup via {@code source_event_id}, the inbox itself) is fully
 * built and ready to accept fan-out the moment such a query exists.
 */
@SpringBootApplication
@EnableConfigurationProperties({MembershipAffiliationServiceProperties.class, FundraisingServiceProperties.class})
public class NotificationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(NotificationServiceApplication.class, args);
  }
}
