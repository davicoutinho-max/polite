package dev.civicpulse.feedcontent.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HashtagExtractorTest {

  @Test
  void extractsHashtagsAndLowercasesThem() {
    assertThat(HashtagExtractor.extract("Loving this #CleanWater initiative! #Civic")).containsExactlyInAnyOrder("cleanwater", "civic");
  }

  @Test
  void deduplicatesRepeatedHashtags() {
    assertThat(HashtagExtractor.extract("#civic is great, really #civic")).containsExactly("civic");
  }

  @Test
  void returnsEmptySetForNullOrBlankContent() {
    assertThat(HashtagExtractor.extract(null)).isEmpty();
    assertThat(HashtagExtractor.extract("  ")).isEmpty();
  }

  @Test
  void returnsEmptySetWhenNoHashtagsPresent() {
    assertThat(HashtagExtractor.extract("just plain text")).isEmpty();
  }
}
