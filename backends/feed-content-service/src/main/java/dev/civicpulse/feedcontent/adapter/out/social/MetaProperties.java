package dev.civicpulse.feedcontent.adapter.out.social;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code appId}/{@code appSecret} are deliberately never given real defaults — they're read from
 * the {@code META_APP_ID}/{@code META_APP_SECRET} environment variables (see
 * feed-content-service/.env, gitignored) so the real credentials never land in a committed file.
 * Create the app at developers.facebook.com, add the "Facebook Login" product, and register {@code
 * redirectUri} exactly (scheme+host+path must match byte-for-byte) as one of its valid OAuth
 * redirect URIs. Production use of {@code pages_manage_posts}/{@code instagram_content_publish}
 * requires Meta's App Review — until then the app works only for its own developers/testers added
 * in the Meta dashboard. See MetaOAuthAdapter/FacebookPublisherAdapter/InstagramPublisherAdapter
 * for what happens when {@code appId} is blank. */
@ConfigurationProperties(prefix = "feed.social.meta")
public record MetaProperties(String appId, String appSecret, String redirectUri, String graphApiVersion) {}
