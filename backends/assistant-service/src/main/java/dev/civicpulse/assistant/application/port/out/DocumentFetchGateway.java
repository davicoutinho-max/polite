package dev.civicpulse.assistant.application.port.out;

/** Downloads a previously-uploaded attachment (feed-content-service's MinIO-backed media store,
 * see MediaController) so its bytes can be handed to Gemini for document understanding. */
public interface DocumentFetchGateway {

  /** Throws {@link dev.civicpulse.assistant.domain.exception.AiUnavailableException} if the
   * document can't be downloaded — treated the same as the AI itself being unreachable, since
   * verification can't proceed either way. */
  Document fetch(String url);

  record Document(byte[] bytes, String mimeType) {}
}
