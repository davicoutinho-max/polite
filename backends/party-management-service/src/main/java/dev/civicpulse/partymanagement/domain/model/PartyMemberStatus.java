package dev.civicpulse.partymanagement.domain.model;

/** Mirrors {@code party_member_status_options}. */
public enum PartyMemberStatus {
  ACTIVE("active"),
  SUSPENDED("suspended");

  private final String code;

  PartyMemberStatus(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static PartyMemberStatus fromCode(String code) {
    for (PartyMemberStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown party_member_status code: " + code);
  }
}
