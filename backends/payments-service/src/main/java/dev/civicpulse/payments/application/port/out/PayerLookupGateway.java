package dev.civicpulse.payments.application.port.out;

import java.util.UUID;

/** Fetches the real, decrypted name/CPF-CNPJ identity-service holds for an account — needed
 * because the real payment gateway (Asaas) legally requires the payer's document number to
 * create a customer record (Brazilian KYC/tax rules), which this service never stores itself. See
 * identity-service's {@code AccountController.getPaymentProfile} for the internal-only endpoint
 * this calls. */
public interface PayerLookupGateway {

  PayerInfo getPaymentProfile(UUID accountId);

  record PayerInfo(String name, String documentNumber) {}
}
