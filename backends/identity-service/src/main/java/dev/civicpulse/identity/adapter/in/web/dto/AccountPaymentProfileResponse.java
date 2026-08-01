package dev.civicpulse.identity.adapter.in.web.dto;

import dev.civicpulse.identity.application.port.in.GetAccountUseCase.PaymentProfile;

/** {@code documentNumber} is the real, decrypted CPF/CNPJ — see GetAccountUseCase.getPaymentProfile's
 * javadoc for why this exists. Backs two callers: payments-service's internal (never
 * Gateway-routed) lookup by arbitrary id, and this service's own self-service
 * {@code GET /accounts/me/document-profile} (Gateway-routed, but the id always comes from the
 * caller's own validated session header — never a path/body parameter, so it can only ever
 * return the caller's own document). */
public record AccountPaymentProfileResponse(String name, String documentNumber) {

  public static AccountPaymentProfileResponse from(PaymentProfile profile) {
    return new AccountPaymentProfileResponse(profile.name(), profile.documentNumber());
  }
}
