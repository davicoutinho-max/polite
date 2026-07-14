package dev.civicpulse.identity.application.port.in;

public interface AuthenticateUseCase {

  AuthResult login(LoginCommand command);

  record LoginCommand(String email, String rawPassword, String userAgent, String ipAddress) {}
}
