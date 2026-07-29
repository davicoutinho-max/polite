package dev.civicpulse.identity.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AesDocumentCipherAdapterTest {

  // Same dev-only default as application.yml's identity.document-cipher.master-key-base64.
  private static final String TEST_KEY_BASE64 = "h8snMZgAXuNXxEtaSfdks61jrzc4ccnwtNkKpVuFOvY=";

  private final AesDocumentCipherAdapter adapter = new AesDocumentCipherAdapter(TEST_KEY_BASE64);

  @Test
  void encryptThenDecryptRecoversTheOriginalDocumentNumber() {
    byte[] encrypted = adapter.encrypt("52998224725");

    assertThat(adapter.decrypt(encrypted)).isEqualTo("52998224725");
  }

  @Test
  void encryptingTheSameValueTwiceProducesDifferentCiphertextButDecryptsTheSame() {
    byte[] first = adapter.encrypt("52998224725");
    byte[] second = adapter.encrypt("52998224725");

    assertThat(first).isNotEqualTo(second); // random IV per call
    assertThat(adapter.decrypt(first)).isEqualTo(adapter.decrypt(second));
  }

  @Test
  void hashIsStableForTheSameInput() {
    assertThat(adapter.hash("52998224725")).isEqualTo(adapter.hash("52998224725"));
  }
}
