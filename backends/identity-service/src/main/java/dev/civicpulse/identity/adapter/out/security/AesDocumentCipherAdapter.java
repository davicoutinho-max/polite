package dev.civicpulse.identity.adapter.out.security;

import dev.civicpulse.identity.application.port.out.DocumentCipher;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code document_number_hash} (SHA-256, one-way, for the UNIQUE constraint) and
 * {@code document_number_encrypted} (AES-256-GCM, recoverable) — see the "PII stays
 * hashed/encrypted" rule in docs/architecture/data-architecture.html.
 *
 * <p>The AES key here is a single master key from configuration, which is the honest local-dev
 * stand-in for real envelope encryption: production must wrap a per-record data-encryption-key
 * with a KMS-managed key (AWS KMS / GCP KMS / Vault transit) instead of using one static key
 * directly, but the port/adapter boundary is identical either way — only this class changes.
 */
@Component
public class AesDocumentCipherAdapter implements DocumentCipher {

  private static final int GCM_TAG_LENGTH_BITS = 128;
  private static final int GCM_IV_LENGTH_BYTES = 12;

  private final SecretKeySpec keySpec;
  private final SecureRandom secureRandom = new SecureRandom();

  public AesDocumentCipherAdapter(@Value("${identity.document-cipher.master-key-base64}") String masterKeyBase64) {
    byte[] keyBytes = Base64.getDecoder().decode(masterKeyBase64);
    if (keyBytes.length != 32) {
      throw new IllegalStateException("identity.document-cipher.master-key-base64 must decode to exactly 32 bytes (AES-256)");
    }
    this.keySpec = new SecretKeySpec(keyBytes, "AES");
  }

  @Override
  public String hash(String rawDocumentNumber) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(rawDocumentNumber.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hashed);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  @Override
  public byte[] encrypt(String rawDocumentNumber) {
    try {
      byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
      byte[] ciphertext = cipher.doFinal(rawDocumentNumber.getBytes(StandardCharsets.UTF_8));

      // Store IV alongside the ciphertext (IV need not be secret, only unique per encryption).
      return ByteBuffer.allocate(iv.length + ciphertext.length).put(iv).put(ciphertext).array();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to encrypt document number", e);
    }
  }
}
