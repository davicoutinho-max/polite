package dev.civicpulse.feedcontent.application.port.out;

import java.util.List;
import java.util.Optional;

/** Real Meta Graph API OAuth — one login covers both Facebook Page publishing and Instagram
 * publishing (Instagram posting rides on a Facebook Page's linked Instagram Business account),
 * so this single gateway backs connecting both platforms at once. Requires a real Meta app
 * (developers.facebook.com) with {@code pages_manage_posts}/{@code pages_show_list}/{@code
 * instagram_content_publish} permissions approved for production use — see MetaProperties'
 * javadoc for the credentials this needs. */
public interface MetaOAuthGateway {

  /** The URL to send the citizen's browser to for the Meta consent screen. {@code state} must be
   * echoed back on the callback unmodified — it's how {@link
   * dev.civicpulse.feedcontent.application.ManageSocialConnectionService} maps the callback back
   * to the account that started the flow. */
  String buildAuthorizeUrl(String state);

  /** Throws {@link dev.civicpulse.feedcontent.domain.exception.SocialOAuthException} if the code
   * is invalid/expired or the Graph API call itself fails. */
  MetaAuthResult exchangeCode(String code);

  /** One Facebook Page the citizen manages, with its own long-lived Page Access Token (used to
   * post to the Page's feed) and — when present — the id of the Instagram Business account
   * linked to that Page (used to post to Instagram via the same token). */
  record MetaPage(String pageId, String pageName, String pageAccessToken, Optional<String> instagramBusinessAccountId) {}

  record MetaAuthResult(List<MetaPage> pages) {}
}
