package dev.civicpulse.governmentsync.application.support;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Real CPF/CNPJ numbers are frequently unavailable from the government open-data APIs this
 * service consumes — confirmed absent for every senator, absent entirely for party CNPJ, and
 * missing for individual deputies whenever the per-record detail lookup fails. Synced accounts
 * (see identity-service's {@code Account.registerSynced}/{@code AuthenticateService}) never
 * support login, so a deterministically-derived placeholder digit string is safe to store in
 * identity-service's document-number-hash uniqueness index in place of a real one — it must never
 * be displayed anywhere as an actual CPF/CNPJ. Deterministic (not random) so re-running the sync
 * against the same external record always yields the same placeholder, which is required for the
 * upsert-by-{@code externalSource}/{@code externalId} idempotency this whole pipeline depends on. */
public final class DocumentNumberFallback {

  private DocumentNumberFallback() {}

  public static String synthesize(String seed, int digitCount) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(seed.getBytes(StandardCharsets.UTF_8));
      BigInteger modulus = BigInteger.TEN.pow(digitCount);
      BigInteger value = new BigInteger(1, hash).mod(modulus);
      return String.format("%0" + digitCount + "d", value);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 must always be available on the JVM", e);
    }
  }

  /** Same deterministic-placeholder idea as {@link #synthesize}, but for {@code PartyRegistryEntry
   * .number} — an {@code int}, not a fixed-digit-count document string. Used when a party's real
   * TSE electoral number isn't available from whichever source is syncing it (confirmed null even
   * for well-known parties in the Câmara API — see SyncFederalLegislatureService). The
   * [base, base+range) window should sit well above any real electoral number (small, 2-3 digit
   * values) so a placeholder can never collide with — or be mistaken for — a genuine one. */
  public static int syntheticNumber(String seed, int base, int range) {
    return base + Integer.parseInt(synthesize(seed, 6)) % range;
  }
}
