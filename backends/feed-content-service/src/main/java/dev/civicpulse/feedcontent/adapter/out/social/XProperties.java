package dev.civicpulse.feedcontent.adapter.out.social;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code clientId}/{@code clientSecret} are deliberately never given real defaults — they're read
 * from the {@code X_CLIENT_ID}/{@code X_CLIENT_SECRET} environment variables (see
 * feed-content-service/.env, gitignored). Create the app at developer.x.com, enable "OAuth 2.0"
 * with type "Confidential client", and register {@code redirectUri} exactly as its callback URL.
 * Posting tweets via the API requires at least X's paid "Basic" access tier — the free tier is
 * read-only, so {@link XPublisherAdapter} will get a real 403 from X until a paid tier is active
 * on the app, even with valid credentials. */
@ConfigurationProperties(prefix = "feed.social.x")
public record XProperties(String clientId, String clientSecret, String redirectUri) {}
