package dev.civicpulse.platformconfig.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface LanguageJpaRepository extends JpaRepository<LanguageJpaEntity, String> {

  Optional<LanguageJpaEntity> findByIsDefaultTrue();

  @Modifying
  @Query("update LanguageJpaEntity l set l.isDefault = false where l.isDefault = true")
  void clearDefaultFlag();

  @Modifying
  @Query("update LanguageJpaEntity l set l.isDefault = true where l.id = :id")
  void markAsDefault(@Param("id") String id);
}
