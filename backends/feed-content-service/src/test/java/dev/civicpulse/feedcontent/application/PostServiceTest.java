package dev.civicpulse.feedcontent.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.feedcontent.application.port.out.EventPublisher;
import dev.civicpulse.feedcontent.application.port.out.PostAgendaDetailsRepository;
import dev.civicpulse.feedcontent.application.port.out.PostHashtagRepository;
import dev.civicpulse.feedcontent.application.port.out.PostMetricsRepository;
import dev.civicpulse.feedcontent.application.port.out.PostRepository;
import dev.civicpulse.feedcontent.application.port.out.PostTagRepository;
import dev.civicpulse.feedcontent.domain.event.PostPublished;
import dev.civicpulse.feedcontent.domain.model.PostAgendaDetails;
import dev.civicpulse.feedcontent.domain.model.PostHashtag;
import dev.civicpulse.feedcontent.domain.model.PostTag;
import dev.civicpulse.feedcontent.domain.model.PostVisibility;
import dev.civicpulse.feedcontent.domain.model.TagSeverity;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private PostRepository postRepository;
  @Mock private PostAgendaDetailsRepository postAgendaDetailsRepository;
  @Mock private PostTagRepository postTagRepository;
  @Mock private PostMetricsRepository postMetricsRepository;
  @Mock private PostHashtagRepository postHashtagRepository;
  @Mock private EventPublisher eventPublisher;

  private PostService service;

  @BeforeEach
  void setUp() {
    service =
        new PostService(
            postRepository,
            postAgendaDetailsRepository,
            postTagRepository,
            postMetricsRepository,
            postHashtagRepository,
            eventPublisher,
            Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void publishTextPostSavesPostMetricsAndPublishesEvent() {
    when(postRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    UUID authorId = UUID.randomUUID();

    var post = service.publishTextPost(authorId, "hello", null, PostVisibility.PUBLIC, "ctx");

    assertThat(post.authorAccountId()).isEqualTo(authorId);
    assertThat(post.createdAt()).isEqualTo(NOW);
    verify(postMetricsRepository).save(any());
    ArgumentCaptor<PostPublished> captor = ArgumentCaptor.forClass(PostPublished.class);
    verify(eventPublisher).publish(captor.capture());
    assertThat(captor.getValue().postId()).isEqualTo(post.id());
    assertThat(captor.getValue().occurredAt()).isEqualTo(NOW);
  }

  @Test
  void publishAgendaPostAlsoSavesAgendaDetails() {
    when(postRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    var post = service.publishAgendaPost(UUID.randomUUID(), "Town hall", "Aug 12, 2026", "City Hall", PostVisibility.PUBLIC, null);

    ArgumentCaptor<PostAgendaDetails> captor = ArgumentCaptor.forClass(PostAgendaDetails.class);
    verify(postAgendaDetailsRepository).save(captor.capture());
    assertThat(captor.getValue().postId()).isEqualTo(post.id());
    assertThat(captor.getValue().title()).isEqualTo("Town hall");
  }

  @Test
  void publishLivePostCarriesLiveSessionId() {
    when(postRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    UUID liveSessionId = UUID.randomUUID();

    var post = service.publishLivePost(UUID.randomUUID(), liveSessionId, PostVisibility.PUBLIC, null);

    assertThat(post.liveSessionId()).contains(liveSessionId);
  }

  @Test
  void publishTextPostExtractsAndSavesHashtags() {
    when(postRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    UUID authorId = UUID.randomUUID();

    var post = service.publishTextPost(authorId, "Loving this #CleanWater initiative! #civic", null, PostVisibility.PUBLIC, "ctx");

    ArgumentCaptor<PostHashtag> captor = ArgumentCaptor.forClass(PostHashtag.class);
    verify(postHashtagRepository, org.mockito.Mockito.times(2)).save(captor.capture());
    List<String> hashtags = captor.getAllValues().stream().map(PostHashtag::hashtag).toList();
    assertThat(hashtags).containsExactlyInAnyOrder("cleanwater", "civic");
    assertThat(captor.getAllValues()).allSatisfy(h -> assertThat(h.postId()).isEqualTo(post.id()));
  }

  @Test
  void publishTextPostWithNoHashtagsSavesNone() {
    when(postRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.publishTextPost(UUID.randomUUID(), "No tags here", null, PostVisibility.PUBLIC, "ctx");

    verify(postHashtagRepository, org.mockito.Mockito.never()).save(any());
  }

  @Test
  void addTagSavesTagAgainstPost() {
    UUID postId = UUID.randomUUID();

    service.addTag(postId, "#Agenda", TagSeverity.INFO, "event");

    ArgumentCaptor<PostTag> captor = ArgumentCaptor.forClass(PostTag.class);
    verify(postTagRepository).save(captor.capture());
    assertThat(captor.getValue().postId()).isEqualTo(postId);
    assertThat(captor.getValue().label()).isEqualTo("#Agenda");
  }
}
