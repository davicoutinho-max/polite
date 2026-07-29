package dev.civicpulse.identity.application.port.out;

/** {@code accounts.document_number_hash} (SHA-256, for uniqueness checks) and
 * {@code document_number_encrypted} (envelope-encrypted via KMS, recoverable) — see the "PII
 * stays hashed/encrypted" rule in the Data &amp; Persistence dossier. The domain never sees a
 * raw CPF/CNPJ; only this adapter and the caller that captured user input do. */
public interface DocumentCipher {

  String hash(String rawDocumentNumber);

  byte[] encrypt(String rawDocumentNumber);

  /** Recovers the raw CPF/CNPJ from {@code encrypt}'s output — needed only where a real external
   * party legally requires the document number itself (e.g. a payment gateway's KYC/tax
   * requirements when creating a customer record), never for display or logging. Callers must
   * treat the result with the same care as the original raw input. */
  String decrypt(byte[] encryptedDocumentNumber);
}
