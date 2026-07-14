package dev.civicpulse.directory.domain.model;

/** Mirrors the {@code party_spectrum_options} parameter table. */
public enum PartySpectrum {
  LEFT("left"),
  CENTER_LEFT("center_left"),
  CENTER("center"),
  CENTER_RIGHT("center_right"),
  RIGHT("right");

  private final String code;

  PartySpectrum(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static PartySpectrum fromCode(String code) {
    for (PartySpectrum spectrum : values()) {
      if (spectrum.code.equals(code)) {
        return spectrum;
      }
    }
    throw new IllegalArgumentException("Unknown party_spectrum code: " + code);
  }
}
