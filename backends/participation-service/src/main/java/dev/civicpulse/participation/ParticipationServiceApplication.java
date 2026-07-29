package dev.civicpulse.participation;

import dev.civicpulse.participation.adapter.out.client.EmailProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(EmailProperties.class)
public class ParticipationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ParticipationServiceApplication.class, args);
  }
}
