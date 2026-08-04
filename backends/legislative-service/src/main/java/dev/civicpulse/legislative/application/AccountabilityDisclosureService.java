package dev.civicpulse.legislative.application;

import dev.civicpulse.legislative.application.port.in.ManageAccountabilityDisclosureUseCase;
import dev.civicpulse.legislative.application.port.out.AccountabilityDisclosureRepository;
import dev.civicpulse.legislative.application.port.out.DocumentVerificationGateway;
import dev.civicpulse.legislative.domain.model.AccountabilityDisclosure;
import dev.civicpulse.legislative.domain.model.DisclosureStatus;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountabilityDisclosureService implements ManageAccountabilityDisclosureUseCase {

  private final AccountabilityDisclosureRepository repository;
  private final DocumentVerificationGateway documentVerificationGateway;
  private final Clock clock;

  public AccountabilityDisclosureService(
      AccountabilityDisclosureRepository repository, DocumentVerificationGateway documentVerificationGateway, Clock clock) {
    this.repository = repository;
    this.documentVerificationGateway = documentVerificationGateway;
    this.clock = clock;
  }

  @Override
  @Transactional
  public AccountabilityDisclosure submit(
      UUID politicianAccountId,
      String category,
      int periodMonth,
      int periodYear,
      long declaredAmountCents,
      String documentUrl,
      String notes) {
    var result = documentVerificationGateway.verify(documentUrl, categoryLabel(category), declaredAmountCents);
    AccountabilityDisclosure disclosure =
        AccountabilityDisclosure.score(
            politicianAccountId,
            category,
            periodMonth,
            periodYear,
            declaredAmountCents,
            documentUrl,
            result.matches() ? DisclosureStatus.APPROVED : DisclosureStatus.REJECTED,
            result.extractedAmountCents(),
            result.feedback(),
            notes,
            clock.instant());
    return repository.save(disclosure);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AccountabilityDisclosure> listByPolitician(UUID politicianAccountId) {
    return repository.findByPolitician(politicianAccountId);
  }

  /** Human-readable-ish label sent to the AI prompt — category is now a free-text code matching
   * the line items shown on the transparency tab (e.g. "ceap-fuel"), so this just de-slugs it
   * rather than mapping a closed set of enum cases. */
  private static String categoryLabel(String category) {
    return category.replace('-', ' ');
  }
}
