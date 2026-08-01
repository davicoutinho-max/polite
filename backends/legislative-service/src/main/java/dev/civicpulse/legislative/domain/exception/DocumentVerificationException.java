package dev.civicpulse.legislative.domain.exception;

/** The AI reviewer (assistant-service) is unreachable or failed — distinct from a normal
 * rejection (which is a successful, scored answer of "no match"). Callers must not treat this as
 * a rejection; see AccountabilityDisclosureService's javadoc. */
public class DocumentVerificationException extends RuntimeException {

  public DocumentVerificationException(String message, Throwable cause) {
    super(message, cause);
  }
}
