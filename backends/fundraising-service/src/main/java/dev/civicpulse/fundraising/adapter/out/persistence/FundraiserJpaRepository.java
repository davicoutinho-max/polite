package dev.civicpulse.fundraising.adapter.out.persistence;

import dev.civicpulse.fundraising.domain.model.FundraiserCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface FundraiserJpaRepository extends JpaRepository<FundraiserJpaEntity, UUID> {

  @Query("select f from FundraiserJpaEntity f order by f.createdAt desc")
  List<FundraiserJpaEntity> findAllOrderByCreatedAtDesc(Pageable pageable);

  @Query("select f from FundraiserJpaEntity f where f.category = :category order by f.createdAt desc")
  List<FundraiserJpaEntity> findByCategory(@Param("category") FundraiserCategory category, Pageable pageable);
}
