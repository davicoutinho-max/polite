package dev.civicpulse.identity.application.port.in;

import dev.civicpulse.identity.domain.model.DocumentType;
import java.util.Optional;
import java.util.UUID;

/** Read-only pre-check used by the registration flow: "does this CPF/CNPJ already belong to a
 * real politician/party profile a government-data sync built, that nobody has claimed yet?" —
 * exposed as its own step (rather than the silent side-effect {@code RegisterAccountUseCase}
 * already has) so the frontend can show a confirmation ("is this you? import your data?") before
 * anything is created or changed. Only ever finds a match for the slice of politicians whose
 * real CPF this platform actually has on file (federal deputies via Câmara) — everyone else
 * (senators, state/municipal politicians, every party) carries a synthetic document number and
 * must be found instead through directory search, see the frontend's manual-search fallback. */
public interface CheckDocumentUseCase {

  Optional<SyncedAccountPreview> checkDocument(String rawDocumentNumber);

  record SyncedAccountPreview(UUID accountId, String name, String avatarUrl, String accountType) {}
}
