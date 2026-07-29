package dev.civicpulse.identity.adapter.in.web.dto;

import dev.civicpulse.identity.application.port.in.GetAccountUseCase.PaymentProfile;

/** {@code documentNumber} is the real, decrypted CPF/CNPJ — see GetAccountUseCase.getPaymentProfile's
 * javadoc for why this exists and who may call it (internal-only, never routed by the Gateway). */
public record AccountPaymentProfileResponse(String name, String documentNumber) {

  public static AccountPaymentProfileResponse from(PaymentProfile profile) {
    return new AccountPaymentProfileResponse(profile.name(), profile.documentNumber());
  }
}
