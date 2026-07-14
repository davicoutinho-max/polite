package dev.civicpulse.partymanagement.domain.exception;

import dev.civicpulse.partymanagement.domain.model.AffiliationRequestStatus;
import java.util.UUID;

public final class AffiliationRequestNotPendingException extends RuntimeException {

  public AffiliationRequestNotPendingException(UUID requestId, AffiliationRequestStatus currentStatus) {
    super("Affiliation request " + requestId + " is already " + currentStatus.code() + ", not pending");
  }
}
