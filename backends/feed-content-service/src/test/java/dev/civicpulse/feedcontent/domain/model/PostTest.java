package dev.civicpulse.feedcontent.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PostTest {

  @Test
  void publishTextPostKeepsFieldsAsProvided() {
    UUID id = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    Instant now = Instant.parse("2026-01-01T00:00:00Z");

    Post post = Post.publish(id, authorId, PostKind.TEXT, "hello world", "http://img", PostVisibility.PUBLIC, "ctx", null, now);

    assertThat(post.id()).isEqualTo(id);
    assertThat(post.authorAccountId()).isEqualTo(authorId);
    assertThat(post.kind()).isEqualTo(PostKind.TEXT);
    assertThat(post.content()).contains("hello world");
    assertThat(post.imageUrl()).contains("http://img");
    assertThat(post.visibility()).isEqualTo(PostVisibility.PUBLIC);
    assertThat(post.context()).contains("ctx");
    assertThat(post.liveSessionId()).isEmpty();
    assertThat(post.createdAt()).isEqualTo(now);
  }

  @Test
  void publishLivePostCarriesLiveSessionId() {
    UUID liveSessionId = UUID.randomUUID();
    Post post =
        Post.publish(UUID.randomUUID(), UUID.randomUUID(), PostKind.LIVE, null, null, PostVisibility.PUBLIC, null, liveSessionId, Instant.now());

    assertThat(post.liveSessionId()).contains(liveSessionId);
    assertThat(post.content()).isEmpty();
  }

  @Test
  void equalityIsBasedOnId() {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    Post a = Post.publish(id, UUID.randomUUID(), PostKind.TEXT, "a", null, PostVisibility.PUBLIC, null, null, now);
    Post b = Post.reconstitute(id, UUID.randomUUID(), PostKind.TEXT, "b", null, PostVisibility.PRIVATE, null, null, now);

    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }
}
