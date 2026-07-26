package dev.civicpulse.participation.application.port.in;

import java.time.LocalDate;

/** Everything captured in the signature wizard's first step, before the code/identity check —
 * shape is a superset of both tiers' requirements (see {@code Petition.petitionType()}); the
 * fields a given tier doesn't use are simply left null by the caller. */
public record StartSignatureCommand(
    String fullName,
    String cpf,
    LocalDate birthDate,
    String city,
    String state,
    String verificationMethod,
    String contact,
    String electoralData,
    boolean eSignatureConsent,
    String typedSignature) {}
