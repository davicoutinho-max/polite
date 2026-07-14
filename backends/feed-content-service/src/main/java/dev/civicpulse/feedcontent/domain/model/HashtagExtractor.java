package dev.civicpulse.feedcontent.domain.model;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pure, framework-free extraction of {@code #hashtag} tokens from a text post's content — see
 * post_hashtags' table comment in schema.sql. */
public final class HashtagExtractor {

  private static final Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)");

  private HashtagExtractor() {
    throw new AssertionError();
  }

  public static Set<String> extract(String content) {
    if (content == null || content.isBlank()) {
      return Set.of();
    }
    Set<String> hashtags = new LinkedHashSet<>();
    Matcher matcher = HASHTAG_PATTERN.matcher(content);
    while (matcher.find()) {
      hashtags.add(matcher.group(1).toLowerCase(Locale.ROOT));
    }
    return hashtags;
  }
}
