package dev.civicpulse.governmentsync.application.support;

import java.text.Normalizer;
import java.util.regex.Pattern;

/** Politician handles must be unique per identity-service's {@code existsByHandle} check, but
 * common Brazilian names collide often (multiple "João Silva"s across 27 states) — appending the
 * source's own stable external id guarantees uniqueness without needing a lookup here. */
public final class HandleSlugGenerator {

  private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");
  private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
  private static final Pattern EDGE_HYPHENS = Pattern.compile("(^-|-$)");

  private HandleSlugGenerator() {}

  public static String slugify(String name, String externalId) {
    String withoutDiacritics = DIACRITICS.matcher(Normalizer.normalize(name, Normalizer.Form.NFD)).replaceAll("");
    String slug = EDGE_HYPHENS.matcher(NON_ALNUM.matcher(withoutDiacritics.toLowerCase()).replaceAll("-")).replaceAll("");
    return slug + "-" + externalId;
  }
}
