package dev.civicpulse.assistant.adapter.out.client;

import dev.civicpulse.assistant.application.port.out.DocumentFetchGateway;
import dev.civicpulse.assistant.domain.exception.AiUnavailableException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class DocumentFetchAdapter implements DocumentFetchGateway {

  private final RestClient restClient;

  DocumentFetchAdapter(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.build();
  }

  @Override
  public Document fetch(String url) {
    try {
      var response = restClient.get().uri(url).retrieve().toEntity(byte[].class);
      byte[] bytes = response.getBody();
      if (bytes == null || bytes.length == 0) {
        throw new AiUnavailableException("The attached document could not be downloaded for review.");
      }
      MediaType contentType = response.getHeaders().getContentType();
      String mimeType = contentType != null ? contentType.toString() : "application/octet-stream";
      return new Document(bytes, mimeType);
    } catch (RestClientException e) {
      throw new AiUnavailableException("The attached document could not be downloaded for review.", e);
    }
  }
}
