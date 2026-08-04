package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.AccountabilityDisclosure;
import org.springframework.stereotype.Component;

@Component
class AccountabilityDisclosureMapper {

  AccountabilityDisclosure toDomain(AccountabilityDisclosureJpaEntity entity) {
    return AccountabilityDisclosure.reconstitute(
        entity.getId(),
        entity.getPoliticianAccountId(),
        entity.getCategory(),
        entity.getPeriodMonth(),
        entity.getPeriodYear(),
        entity.getDeclaredAmountCents(),
        entity.getDocumentUrl(),
        entity.getStatus(),
        entity.getExtractedAmountCents(),
        entity.getAiFeedback(),
        entity.getNotes(),
        entity.getSubmittedAt());
  }

  AccountabilityDisclosureJpaEntity toEntity(AccountabilityDisclosure disclosure) {
    return new AccountabilityDisclosureJpaEntity(
        disclosure.id(),
        disclosure.politicianAccountId(),
        disclosure.category(),
        disclosure.periodMonth(),
        disclosure.periodYear(),
        disclosure.declaredAmountCents(),
        disclosure.documentUrl(),
        disclosure.status(),
        disclosure.extractedAmountCents().orElse(null),
        disclosure.aiFeedback(),
        disclosure.notes().orElse(null),
        disclosure.submittedAt());
  }
}
