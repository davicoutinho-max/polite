package dev.civicpulse.partymanagement;

import dev.civicpulse.partymanagement.adapter.out.client.IdentityServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IdentityServiceProperties.class)
public class PartyManagementServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PartyManagementServiceApplication.class, args);
  }
}
