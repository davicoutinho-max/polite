package dev.civicpulse.platformconfig;

import dev.civicpulse.platformconfig.adapter.out.client.IdentityServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IdentityServiceProperties.class)
public class PlatformConfigurationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PlatformConfigurationServiceApplication.class, args);
  }
}
