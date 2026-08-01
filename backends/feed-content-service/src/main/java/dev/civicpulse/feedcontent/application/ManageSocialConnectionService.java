package dev.civicpulse.feedcontent.application;

import dev.civicpulse.feedcontent.application.port.in.ManageSocialConnectionUseCase;
import dev.civicpulse.feedcontent.application.port.out.MetaOAuthGateway;
import dev.civicpulse.feedcontent.application.port.out.OAuthStateStore;
import dev.civicpulse.feedcontent.application.port.out.SocialConnectionRepository;
import dev.civicpulse.feedcontent.application.port.out.XOAuthGateway;
import dev.civicpulse.feedcontent.domain.exception.SocialOAuthException;
import dev.civicpulse.feedcontent.domain.model.SocialConnection;
import dev.civicpulse.feedcontent.domain.model.SocialPlatform;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManageSocialConnectionService implements ManageSocialConnectionUseCase {

  private final SocialConnectionRepository connectionRepository;
  private final MetaOAuthGateway metaOAuthGateway;
  private final XOAuthGateway xOAuthGateway;
  private final OAuthStateStore stateStore;
  private final Clock clock;

  public ManageSocialConnectionService(
      SocialConnectionRepository connectionRepository,
      MetaOAuthGateway metaOAuthGateway,
      XOAuthGateway xOAuthGateway,
      OAuthStateStore stateStore,
      Clock clock) {
    this.connectionRepository = connectionRepository;
    this.metaOAuthGateway = metaOAuthGateway;
    this.xOAuthGateway = xOAuthGateway;
    this.stateStore = stateStore;
    this.clock = clock;
  }

  @Override
  public String startMetaConnect(UUID accountId) {
    String state = UUID.randomUUID().toString();
    stateStore.save(state, accountId, null);
    return metaOAuthGateway.buildAuthorizeUrl(state);
  }

  @Override
  public String startXConnect(UUID accountId) {
    String state = UUID.randomUUID().toString();
    var request = xOAuthGateway.buildAuthorizeRequest(state);
    stateStore.save(state, accountId, request.codeVerifier());
    return request.url();
  }

  @Override
  @Transactional
  public List<SocialPlatform> completeMetaConnect(String code, String state) {
    var pending =
        stateStore
            .consume(state)
            .orElseThrow(() -> new SocialOAuthException("This connection attempt expired or was already used — please try again."));
    var result = metaOAuthGateway.exchangeCode(code);
    if (result.pages().isEmpty()) {
      throw new SocialOAuthException("No Facebook Page was found for this account — you need to manage at least one Page to connect.");
    }
    // MVP: only the first Page the citizen manages — see MetaOAuthGateway's javadoc. Multi-page
    // selection is a real gap, deliberately deferred rather than guessed at.
    var page = result.pages().get(0);
    Instant now = clock.instant();
    List<SocialPlatform> connected = new ArrayList<>();
    upsertConnection(pending.accountId(), SocialPlatform.FACEBOOK, page.pageAccessToken(), page.pageId(), page.pageName(), now);
    connected.add(SocialPlatform.FACEBOOK);
    if (page.instagramBusinessAccountId().isPresent()) {
      upsertConnection(
          pending.accountId(), SocialPlatform.INSTAGRAM, page.pageAccessToken(), page.instagramBusinessAccountId().get(), page.pageName(), now);
      connected.add(SocialPlatform.INSTAGRAM);
    }
    return connected;
  }

  @Override
  @Transactional
  public List<SocialPlatform> completeXConnect(String code, String state) {
    var pending =
        stateStore
            .consume(state)
            .orElseThrow(() -> new SocialOAuthException("This connection attempt expired or was already used — please try again."));
    var result = xOAuthGateway.exchangeCode(code, pending.codeVerifier());
    upsertConnection(pending.accountId(), SocialPlatform.X, result.accessToken(), result.userId(), result.username(), clock.instant());
    return List.of(SocialPlatform.X);
  }

  private void upsertConnection(
      UUID accountId, SocialPlatform platform, String accessToken, String externalAccountId, String externalAccountName, Instant now) {
    var existing = connectionRepository.findByAccountAndPlatform(accountId, platform);
    if (existing.isPresent()) {
      existing.get().reconnect(accessToken, externalAccountId, externalAccountName, now);
      connectionRepository.save(existing.get());
    } else {
      connectionRepository.save(SocialConnection.connect(accountId, platform, accessToken, externalAccountId, externalAccountName, now));
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<SocialConnection> listConnections(UUID accountId) {
    return connectionRepository.findByAccount(accountId);
  }

  @Override
  @Transactional
  public void disconnect(UUID accountId, SocialPlatform platform) {
    connectionRepository.deleteByAccountAndPlatform(accountId, platform);
  }
}
