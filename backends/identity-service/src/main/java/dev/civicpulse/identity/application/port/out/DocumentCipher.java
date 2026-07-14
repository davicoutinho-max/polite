package dev.civicpulse.identity.application.port.out;

/** {@code accounts.document_number_hash} (SHA-256, for uniqueness checks) and
 * {@code document_number_encrypted} (envelope-encrypted via KMS, recoverable) — see the "PII
 * stays hashed/encrypted" rule in the Data &amp; Persistence dossier. The domain never sees a
 * raw CPF/CNPJ; only this adapter and the caller that captured user input do. */
public interface DocumentCipher {

  String hash(String rawDocumentNumber);

  byte[] encrypt(String rawDocumentNumber);
}
