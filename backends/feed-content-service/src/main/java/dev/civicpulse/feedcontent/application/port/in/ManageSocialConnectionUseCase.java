package dev.civicpulse.feedcontent.application.port.in;

import dev.civicpulse.feedcontent.domain.model.SocialConnection;
import dev.civicpulse.feedcontent.domain.model.SocialPlatform;
import java.util.List;
import java.util.UUID;

public interface ManageSocialConnectionUseCase {

  /** Returns the URL to send the citizen's browser to — one Meta login covers both Facebook and
   * Instagram, see MetaOAuthGateway's javadoc. */
  String startMetaConnect(UUID accountId);

  String startXConnect(UUID accountId);

  /** Called from the public callback endpoint after Meta redirects back. Returns which
   * platform(s) actually got connected (Facebook always, Instagram only if the Page has one
   * linked) so the caller can reflect that back to the citizen. */
  List<SocialPlatform> completeMetaConnect(String code, String state);

  List<SocialPlatform> completeXConnect(String code, String state);

  List<SocialConnection> listConnections(UUID accountId);

  void disconnect(UUID accountId, SocialPlatform platform);
}
