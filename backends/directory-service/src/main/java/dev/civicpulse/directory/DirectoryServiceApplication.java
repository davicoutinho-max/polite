package dev.civicpulse.directory;

import dev.civicpulse.directory.adapter.out.client.IdentityServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IdentityServiceProperties.class)
public class DirectoryServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(DirectoryServiceApplication.class, args);
  }
}
