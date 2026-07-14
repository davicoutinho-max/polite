package dev.civicpulse.feedcontent.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.feedcontent.application.port.out.CommentRepository;
import dev.civicpulse.feedcontent.application.port.out.LikeRepository;
import dev.civicpulse.feedcontent.application.port.out.PostAgendaDetailsRepository;
import dev.civicpulse.feedcontent.application.port.out.PostHashtagRepository;
import dev.civicpulse.feedcontent.application.port.out.PostMetricsRepository;
import dev.civicpulse.feedcontent.application.port.out.PostRepository;
import dev.civicpulse.feedcontent.application.port.out.PostTagRepository;
import dev.civicpulse.feedcontent.application.port.out.TrendingTopicCacheRepository;
import dev.civicpulse.feedcontent.domain.model.Comment;
import dev.civicpulse.feedcontent.domain.model.Like;
import dev.civicpulse.feedcontent.domain.model.Post;
import dev.civicpulse.feedcontent.domain.model.PostAgendaDetails;
import dev.civicpulse.feedcontent.domain.model.PostHashtag;
import dev.civicpulse.feedcontent.domain.model.PostKind;
import dev.civicpulse.feedcontent.domain.model.PostMetrics;
import dev.civicpulse.feedcontent.domain.model.PostTag;
import dev.civicpulse.feedcontent.domain.model.PostVisibility;
import dev.civicpulse.feedcontent.domain.model.TagSeverity;
import dev.civicpulse.feedcontent.domain.model.TrendingTopic;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Exercises the real JPA/Postgres adapters against the shared local-dev database (see
 * identity-service's equivalent test for the rationale on why this isn't Testcontainers). */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5432/feed_content_service",
      "spring.datasource.username=feed_content_service_app",
      "spring.datasource.password=feed_dev_pw"
    })
class FeedContentRepositoryIntegrationTest {

  @BeforeAll
  static void requireLocalPostgres() {
    boolean reachable;
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("localhost", 5432), 500);
      reachable = true;
    } catch (Exception e) {
      reachable = false;
    }
    assumeTrue(reachable, "Shared dev Postgres (localhost:5432) is not running — start it with "
        + "'docker compose up -d postgres' in backends/ to run this test");
  }

  @Autowired private PostRepository postRepository;
  @Autowired private PostAgendaDetailsRepository postAgendaDetailsRepository;
  @Autowired private PostTagRepository postTagRepository;
  @Autowired private LikeRepository likeRepository;
  @Autowired private CommentRepository commentRepository;
  @Autowired private PostMetricsRepository postMetricsRepository;
  @Autowired private PostHashtagRepository postHashtagRepository;
  @Autowired private TrendingTopicCacheRepository trendingTopicCacheRepository;

  @Test
  void savesAndRetrievesTextPost() {
    UUID id = UUID.randomUUID();
    Post post = Post.publish(id, UUID.randomUUID(), PostKind.TEXT, "hello world", null, PostVisibility.PUBLIC, "ctx", null, Instant.now());

    postRepository.save(post);

    assertThat(postRepository.findById(id)).isPresent().get().satisfies(found -> assertThat(found.content()).contains("hello world"));
  }

  @Test
  void agendaDetailsRoundTrip() {
    UUID postId = UUID.randomUUID();
    postRepository.save(Post.publish(postId, UUID.randomUUID(), PostKind.AGENDA, null, null, PostVisibility.PUBLIC, null, null, Instant.now()));

    postAgendaDetailsRepository.save(PostAgendaDetails.create(postId, "Town hall", "Aug 12, 2026 - 14:00", "City Hall"));

    assertThat(postAgendaDetailsRepository.findByPostId(postId))
        .isPresent()
        .get()
        .satisfies(found -> assertThat(found.title()).isEqualTo("Town hall"));
  }

  @Test
  void postTagPersistsAndListsByPost() {
    UUID postId = UUID.randomUUID();
    postRepository.save(Post.publish(postId, UUID.randomUUID(), PostKind.TEXT, "tagged", null, PostVisibility.PUBLIC, null, null, Instant.now()));

    postTagRepository.save(PostTag.add(postId, "#Agenda", TagSeverity.INFO, "event"));

    assertThat(postTagRepository.findByPostId(postId)).anySatisfy(tag -> assertThat(tag.label()).isEqualTo("#Agenda"));
  }

  @Test
  void likeExistsReflectsSavedState() {
    UUID postId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    postRepository.save(Post.publish(postId, UUID.randomUUID(), PostKind.TEXT, "likeme", null, PostVisibility.PUBLIC, null, null, Instant.now()));

    assertThat(likeRepository.exists(postId, accountId)).isFalse();

    likeRepository.save(Like.create(postId, accountId, Instant.now()));

    assertThat(likeRepository.exists(postId, accountId)).isTrue();

    likeRepository.delete(postId, accountId);

    assertThat(likeRepository.exists(postId, accountId)).isFalse();
  }

  @Test
  void commentPersistsAndListsInCreationOrder() {
    UUID postId = UUID.randomUUID();
    postRepository.save(Post.publish(postId, UUID.randomUUID(), PostKind.TEXT, "commented", null, PostVisibility.PUBLIC, null, null, Instant.now()));

    commentRepository.save(Comment.add(UUID.randomUUID(), postId, UUID.randomUUID(), "first!", Instant.now()));

    assertThat(commentRepository.findByPostId(postId)).anySatisfy(c -> assertThat(c.body()).isEqualTo("first!"));
  }

  @Test
  void postMetricsPersistsCounters() {
    UUID postId = UUID.randomUUID();
    postRepository.save(Post.publish(postId, UUID.randomUUID(), PostKind.TEXT, "counted", null, PostVisibility.PUBLIC, null, null, Instant.now()));
    PostMetrics metrics = PostMetrics.initial(postId, Instant.now());
    metrics.incrementLikes(Instant.now());

    postMetricsRepository.save(metrics);

    assertThat(postMetricsRepository.findByPostId(postId)).isPresent().get().satisfies(found -> assertThat(found.likesCount()).isEqualTo(1));
  }

  @Test
  void postHashtagCountsWithinTheLookbackWindow() {
    UUID postId = UUID.randomUUID();
    Instant now = Instant.now();
    String hashtag = "civic" + System.nanoTime();
    postHashtagRepository.save(PostHashtag.add(postId, hashtag, now));

    List<PostHashtagRepository.HashtagCount> counts = postHashtagRepository.countByHashtagSince(now.minus(Duration.ofHours(24)), 10);

    assertThat(counts).anySatisfy(c -> assertThat(c.hashtag()).isEqualTo(hashtag));
  }

  @Test
  void trendingTopicCacheReplaceAllOverwritesPreviousContents() {
    Instant now = Instant.now();
    trendingTopicCacheRepository.replaceAll(List.of(new TrendingTopic("firsttag", 5, (short) 1, now)));
    assertThat(trendingTopicCacheRepository.findTopRanked(10)).extracting(TrendingTopic::hashtag).contains("firsttag");

    trendingTopicCacheRepository.replaceAll(List.of(new TrendingTopic("secondtag", 9, (short) 1, now)));

    assertThat(trendingTopicCacheRepository.findTopRanked(10)).extracting(TrendingTopic::hashtag).containsExactly("secondtag");
  }
}
