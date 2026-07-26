package dev.civicpulse.governmentsync.application.support;

import java.util.Locale;
import java.util.Set;

/** TSE's bulk datasets give every name in ALL CAPS (confirmed: "LUCIANO BISPO DE LIMA"), unlike
 * Câmara/Senado's APIs which already return properly-cased names — displaying TSE-sourced
 * politicians in shouty caps next to everyone else would be a jarring, obviously-wrong-looking
 * inconsistency on the same directory page. */
public final class PortugueseNameCasing {

  private static final Set<String> LOWERCASE_CONNECTORS = Set.of("de", "da", "do", "das", "dos", "e");
  private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

  private PortugueseNameCasing() {}

  public static String titleCase(String name) {
    if (name == null || name.isBlank()) {
      return name;
    }
    String[] words = name.toLowerCase(PT_BR).trim().split("\\s+");
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < words.length; i++) {
      String word = words[i];
      if (i > 0 && LOWERCASE_CONNECTORS.contains(word)) {
        result.append(word);
      } else {
        result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
      }
      if (i < words.length - 1) {
        result.append(' ');
      }
    }
    return result.toString();
  }
}
