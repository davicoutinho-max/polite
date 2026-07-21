package dev.civicpulse.directory.adapter.out.persistence;

import dev.civicpulse.directory.domain.model.GovLevel;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PoliticianJpaRepository extends JpaRepository<PoliticianJpaEntity, UUID> {

  @Query(
      "select p from PoliticianJpaEntity p "
          + "where (:state is null or p.state = :state) "
          + "and (:level is null or p.level = :level) "
          + "and (:partyId is null or p.partyId = :partyId) "
          + "order by p.name asc")
  List<PoliticianJpaEntity> search(
      @Param("state") String state, @Param("level") GovLevel level, @Param("partyId") UUID partyId, Pageable pageable);

  @Modifying
  @Query("update PoliticianJpaEntity p set p.partyId = :partyId, p.partyAcronym = :partyAcronym, p.updatedAt = :now where p.accountId = :accountId")
  void assignParty(@Param("accountId") UUID accountId, @Param("partyId") UUID partyId, @Param("partyAcronym") String partyAcronym, @Param("now") Instant now);

  @Modifying
  @Query("update PoliticianJpaEntity p set p.office = :office, p.state = :state, p.updatedAt = :now where p.accountId = :accountId")
  void assignOffice(
      @Param("accountId") UUID accountId, @Param("office") String office, @Param("state") String state, @Param("now") Instant now);

  @Modifying
  @Query(
      value =
          "insert into politicians (account_id, name, handle, avatar_url, verified, followers_count, bills_count, updated_at) "
              + "values (:accountId, :name, :handle, :avatarUrl, false, 0, 0, :now) "
              + "on conflict (account_id) do nothing",
      nativeQuery = true)
  void createIfAbsent(
      @Param("accountId") UUID accountId,
      @Param("name") String name,
      @Param("handle") String handle,
      @Param("avatarUrl") String avatarUrl,
      @Param("now") Instant now);
}
