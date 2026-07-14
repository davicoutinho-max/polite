package dev.civicpulse.identity.adapter.out.persistence;

import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import org.springframework.stereotype.Component;

@Component
class AccountMapper {

  Account toDomain(AccountJpaEntity entity) {
    return Account.reconstitute(
        AccountId.of(entity.getId()),
        entity.getAccountType(),
        entity.getName(),
        entity.getHandle(),
        entity.getEmail(),
        entity.getPasswordHash(),
        entity.getDocumentType(),
        entity.getDocumentNumberHash(),
        entity.getDocumentNumberEncrypted(),
        entity.isVerified(),
        entity.getAnonymizedAt(),
        entity.getAvatarUrl(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  AccountJpaEntity toEntity(Account account) {
    return new AccountJpaEntity(
        account.id().value(),
        account.accountType(),
        account.name(),
        account.handle(),
        account.email(),
        account.passwordHash(),
        account.documentType().orElse(null),
        account.documentNumberHash().orElse(null),
        account.documentNumberEncrypted().orElse(null),
        account.verified(),
        account.anonymizedAt().orElse(null),
        account.avatarUrl().orElse(null),
        account.createdAt(),
        account.updatedAt());
  }
}
