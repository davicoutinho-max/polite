package dev.civicpulse.feedcontent.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface LikeJpaRepository extends JpaRepository<LikeJpaEntity, LikeId> {}
