package dev.civicpulse.feedcontent.application;

import dev.civicpulse.feedcontent.application.port.in.PublishPostUseCase;
import dev.civicpulse.feedcontent.application.port.out.EventPublisher;
import dev.civicpulse.feedcontent.application.port.out.PostAgendaDetailsRepository;
import dev.civicpulse.feedcontent.application.port.out.PostHashtagRepository;
import dev.civicpulse.feedcontent.application.port.out.PostMetricsRepository;
import dev.civicpulse.feedcontent.application.port.out.PostRepository;
import dev.civicpulse.feedcontent.application.port.out.PostTagRepository;
import dev.civicpulse.feedcontent.domain.event.PostPublished;
import dev.civicpulse.feedcontent.domain.model.HashtagExtractor;
import dev.civicpulse.feedcontent.domain.model.Post;
import dev.civicpulse.feedcontent.domain.model.PostAgendaDetails;
import dev.civicpulse.feedcontent.domain.model.PostHashtag;
import dev.civicpulse.feedcontent.domain.model.PostKind;
import dev.civicpulse.feedcontent.domain.model.PostMetrics;
import dev.civicpulse.feedcontent.domain.model.PostTag;
import dev.civicpulse.feedcontent.domain.model.PostVisibility;
import dev.civicpulse.feedcontent.domain.model.TagSeverity;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService implements PublishPostUseCase {

  private final PostRepository postRepository;
  private final PostAgendaDetailsRepository postAgendaDetailsRepository;
  private final PostTagRepository postTagRepository;
  private final PostMetricsRepository postMetricsRepository;
  private final PostHashtagRepository postHashtagRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public PostService(
      PostRepository postRepository,
      PostAgendaDetailsRepository postAgendaDetailsRepository,
      PostTagRepository postTagRepository,
      PostMetricsRepository postMetricsRepository,
      PostHashtagRepository postHashtagRepository,
      EventPublisher eventPublisher,
      Clock clock) {
    this.postRepository = postRepository;
    this.postAgendaDetailsRepository = postAgendaDetailsRepository;
    this.postTagRepository = postTagRepository;
    this.postMetricsRepository = postMetricsRepository;
    this.postHashtagRepository = postHashtagRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Post publishTextPost(UUID authorAccountId, String content, String imageUrl, PostVisibility visibility, String context) {
    return publish(authorAccountId, PostKind.TEXT, content, imageUrl, visibility, context, null);
  }

  @Override
  @Transactional
  public Post publishAgendaPost(UUID authorAccountId, String title, String eventDate, String location, PostVisibility visibility, String context) {
    Post post = publish(authorAccountId, PostKind.AGENDA, null, null, visibility, context, null);
    postAgendaDetailsRepository.save(PostAgendaDetails.create(post.id(), title, eventDate, location));
    return post;
  }

  @Override
  @Transactional
  public Post publishLivePost(UUID authorAccountId, UUID liveSessionId, PostVisibility visibility, String context) {
    return publish(authorAccountId, PostKind.LIVE, null, null, visibility, context, liveSessionId);
  }

  @Override
  @Transactional
  public void addTag(UUID postId, String label, TagSeverity severity, String icon) {
    postTagRepository.save(PostTag.add(postId, label, severity, icon));
  }

  private Post publish(
      UUID authorAccountId, PostKind kind, String content, String imageUrl, PostVisibility visibility, String context, UUID liveSessionId) {
    Instant now = clock.instant();
    Post post = Post.publish(UUID.randomUUID(), authorAccountId, kind, content, imageUrl, visibility, context, liveSessionId, now);
    Post saved = postRepository.save(post);
    postMetricsRepository.save(PostMetrics.initial(saved.id(), now));
    for (String hashtag : HashtagExtractor.extract(content)) {
      postHashtagRepository.save(PostHashtag.add(saved.id(), hashtag, now));
    }
    eventPublisher.publish(new PostPublished(saved.id(), authorAccountId, kind.code(), visibility.code(), now));
    return saved;
  }
}
