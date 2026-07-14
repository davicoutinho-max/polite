package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.domain.model.PostVisibility;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PostJpaRepository extends JpaRepository<PostJpaEntity, UUID> {

  @Query("select p from PostJpaEntity p where p.authorAccountId = :authorAccountId order by p.createdAt desc")
  List<PostJpaEntity> findByAuthorAccountId(@Param("authorAccountId") UUID authorAccountId, Pageable pageable);

  @Query("select p from PostJpaEntity p where p.visibility = :visibility order by p.createdAt desc")
  List<PostJpaEntity> findByVisibilityOrderByCreatedAtDesc(@Param("visibility") PostVisibility visibility, Pageable pageable);
}
