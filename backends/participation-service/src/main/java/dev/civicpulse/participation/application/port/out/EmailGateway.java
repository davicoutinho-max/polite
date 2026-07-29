package dev.civicpulse.participation.application.port.out;

/** Anti-corruption-layer boundary for real transactional email — see ResendEmailClient for the
 * implementation and EmailProperties for how the API key is supplied. Unlike most external
 * providers elsewhere in this codebase, this one is NOT a stub: a signature's verification code
 * must actually reach the citizen's inbox for the signature flow to be real, not simulated. */
public interface EmailGateway {

  void sendVerificationCode(String toEmail, String code);
}
