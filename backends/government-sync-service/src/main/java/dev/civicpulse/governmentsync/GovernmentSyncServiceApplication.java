package dev.civicpulse.governmentsync;

import dev.civicpulse.governmentsync.adapter.out.client.CamaraServiceProperties;
import dev.civicpulse.governmentsync.adapter.out.client.ElectionsServiceProperties;
import dev.civicpulse.governmentsync.adapter.out.client.LegislativeServiceProperties;
import dev.civicpulse.governmentsync.adapter.out.client.PartyManagementServiceProperties;
import dev.civicpulse.governmentsync.adapter.out.client.PlatformConfigurationServiceProperties;
import dev.civicpulse.governmentsync.adapter.out.client.SenadoServiceProperties;
import dev.civicpulse.governmentsync.adapter.out.client.TseServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
  CamaraServiceProperties.class,
  SenadoServiceProperties.class,
  PartyManagementServiceProperties.class,
  PlatformConfigurationServiceProperties.class,
  TseServiceProperties.class,
  LegislativeServiceProperties.class,
  ElectionsServiceProperties.class
})
public class GovernmentSyncServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(GovernmentSyncServiceApplication.class, args);
  }
}
