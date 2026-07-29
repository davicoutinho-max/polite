package dev.civicpulse.assistant;

import dev.civicpulse.assistant.adapter.out.client.GeminiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GeminiProperties.class)
public class AssistantServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AssistantServiceApplication.class, args);
  }
}
