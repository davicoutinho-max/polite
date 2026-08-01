package dev.civicpulse.feedcontent;

import dev.civicpulse.feedcontent.adapter.out.social.MetaProperties;
import dev.civicpulse.feedcontent.adapter.out.social.XProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Scope note for this pass: schema.sql documents Redis as the intended hot path for ranked
 * timelines and like/comment counters, and a {@code FollowCreated}/{@code FollowRemoved} Kafka
 * consumer feeding a "Following feed". Neither is implemented here — Postgres alone remains a
 * correct, complete system of record (this service already serves public/by-author feeds and
 * synchronously-consistent counters straight from it); adding Redis or a Following-feed table
 * without a concrete consumer requirement would be speculative. Both are safe, additive follow-ups
 * whenever a real caller needs them.
 */
@SpringBootApplication
@EnableConfigurationProperties({MetaProperties.class, XProperties.class})
public class FeedContentServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(FeedContentServiceApplication.class, args);
  }
}
