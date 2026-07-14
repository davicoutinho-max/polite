package dev.civicpulse.gateway.config;

import dev.civicpulse.gateway.security.JwtValidator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityBeansConfig {

  @Bean
  public JwtValidator jwtValidator(JwtProperties properties) {
    return new JwtValidator(properties.signingKeyBase64());
  }
}
