package dev.civicpulse.legislative;

import dev.civicpulse.legislative.adapter.out.client.AssistantServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AssistantServiceProperties.class)
public class LegislativeServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(LegislativeServiceApplication.class, args);
  }
}
