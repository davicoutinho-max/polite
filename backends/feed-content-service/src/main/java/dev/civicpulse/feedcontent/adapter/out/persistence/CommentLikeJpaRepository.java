package dev.civicpulse.feedcontent.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface CommentLikeJpaRepository extends JpaRepository<CommentLikeJpaEntity, CommentLikeId> {}
