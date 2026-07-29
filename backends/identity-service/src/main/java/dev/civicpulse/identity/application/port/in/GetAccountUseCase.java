package dev.civicpulse.identity.application.port.in;

import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import java.util.Set;

public interface GetAccountUseCase {

  Account getById(AccountId id);

  Set<String> getPermissions(AccountId id);

  /** Internal-only — decrypts the account's real CPF/CNPJ for a caller with a genuine legal need
   * for the raw document number (currently: payments-service creating a customer record with a
   * Brazilian payment gateway, which requires it for KYC/tax purposes). Never call this to
   * display or log the document number. */
  PaymentProfile getPaymentProfile(AccountId id);

  record PaymentProfile(String name, String documentNumber) {}
}
