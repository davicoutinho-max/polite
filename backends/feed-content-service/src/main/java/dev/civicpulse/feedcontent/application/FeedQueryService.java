package dev.civicpulse.feedcontent.application;

import dev.civicpulse.feedcontent.application.port.in.GetFeedUseCase;
import dev.civicpulse.feedcontent.application.port.out.PostMetricsRepository;
import dev.civicpulse.feedcontent.application.port.out.PostRepository;
import dev.civicpulse.feedcontent.domain.exception.PostNotFoundException;
import dev.civicpulse.feedcontent.domain.model.Post;
import dev.civicpulse.feedcontent.domain.model.PostMetrics;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedQueryService implements GetFeedUseCase {

  private final PostRepository postRepository;
  private final PostMetricsRepository postMetricsRepository;

  public FeedQueryService(PostRepository postRepository, PostMetricsRepository postMetricsRepository) {
    this.postRepository = postRepository;
    this.postMetricsRepository = postMetricsRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Post getById(UUID id) {
    return postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public PostMetrics getMetrics(UUID postId) {
    return postMetricsRepository.findByPostId(postId).orElseThrow(() -> new PostNotFoundException(postId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Post> getByAuthor(UUID authorAccountId, int page, int pageSize) {
    return postRepository.findByAuthorAccountId(authorAccountId, page, pageSize);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Post> getPublicFeed(int page, int pageSize) {
    return postRepository.findPublicFeed(page, pageSize);
  }
}
