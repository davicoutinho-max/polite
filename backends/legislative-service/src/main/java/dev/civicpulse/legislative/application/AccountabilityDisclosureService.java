package dev.civicpulse.legislative.application;

import dev.civicpulse.legislative.application.port.in.ManageAccountabilityDisclosureUseCase;
import dev.civicpulse.legislative.application.port.out.AccountabilityDisclosureRepository;
import dev.civicpulse.legislative.application.port.out.DocumentVerificationGateway;
import dev.civicpulse.legislative.domain.model.AccountabilityCategory;
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
  public AccountabilityDisclosure submit(UUID politicianAccountId, AccountabilityCategory category, long declaredAmountCents, String documentUrl) {
    var result = documentVerificationGateway.verify(documentUrl, categoryLabel(category), declaredAmountCents);
    AccountabilityDisclosure disclosure =
        AccountabilityDisclosure.score(
            politicianAccountId,
            category,
            declaredAmountCents,
            documentUrl,
            result.matches() ? DisclosureStatus.APPROVED : DisclosureStatus.REJECTED,
            result.extractedAmountCents(),
            result.feedback(),
            clock.instant());
    return repository.save(disclosure);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AccountabilityDisclosure> listByPolitician(UUID politicianAccountId) {
    return repository.findByPolitician(politicianAccountId);
  }

  /** Human-readable label sent to the AI prompt — mirrors accountability_category_options'
   * seeded labels (see V2 migration) so the model's context matches what's shown on screen. */
  private static String categoryLabel(AccountabilityCategory category) {
    return switch (category) {
      case OFFICE_BUDGET -> "Verba de Gabinete";
      case PARLIAMENTARY_QUOTA -> "Cota para o Exercício da Atividade Parlamentar (CEAP)";
      case PARLIAMENTARY_AMENDMENTS -> "Emendas Parlamentares";
      case TRAVEL_ALLOWANCE -> "Diárias e Passagens";
      case ADVERTISING -> "Publicidade Institucional";
    };
  }
}
