package dev.civicpulse.activityfeed;

import dev.civicpulse.activityfeed.adapter.out.client.FundraisingServiceProperties;
import dev.civicpulse.activityfeed.adapter.out.client.IdentityServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({IdentityServiceProperties.class, FundraisingServiceProperties.class})
public class ActivityFeedServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ActivityFeedServiceApplication.class, args);
  }
}
