package dev.civicpulse.platformconfig.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface TranslationKeyJpaRepository extends JpaRepository<TranslationKeyJpaEntity, UUID> {

  Optional<TranslationKeyJpaEntity> findByKey(String key);
}
