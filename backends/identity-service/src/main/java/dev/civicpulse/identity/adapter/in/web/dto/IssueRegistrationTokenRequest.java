package dev.civicpulse.identity.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** {@code prefillData} is an opaque JSON string the issuing service builds and later decodes
 * itself — identity-service never interprets it, see RegistrationToken's javadoc.
 * {@code issuedByAccountId} is supplied by the caller rather than read from a session header —
 * this endpoint is internal/server-to-server, not gateway-routed with a citizen session. */
public record IssueRegistrationTokenRequest(
    @NotBlank String accountType, @NotNull UUID issuedByAccountId, String targetEmail, String prefillData) {}
