package dev.civicpulse.messaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Scope note: schema.sql restricts group membership to politician/party account types "at the
 * application layer... the picker never offers citizens as addable participants" — this is a
 * UI/caller-side constraint (which candidates the group picker shows), not one this service
 * re-validates against account type, since it holds no account-type data of its own and adding a
 * synchronous call to identity-service purely to police a caller-side concern isn't warranted
 * here (contrast elections-service's PoliticianDirectoryGateway, which exists because this
 * service's own domain model genuinely needs profile data it doesn't have). Real-time presence,
 * typing indicators and unread counters are documented as Redis-backed in schema.sql and are not
 * implemented in this pass — Postgres remains a correct, complete system of record for
 * conversations, participants and message history.
 */
@SpringBootApplication
public class MessagingServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(MessagingServiceApplication.class, args);
  }
}
