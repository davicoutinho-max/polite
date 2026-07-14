package dev.civicpulse.identity.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** A single injectable {@link Clock} bean so application services never call
 * {@code Instant.now()} directly — tests can substitute a fixed clock instead. */
@Configuration
public class ClockConfig {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
