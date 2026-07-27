package dev.civicpulse.governmentsync.adapter.out.client;

import dev.civicpulse.governmentsync.application.port.out.CamaraGateway;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** dadosabertos.camara.leg.br/api/v2 — see CamaraGateway for the confirmed shape/gaps of each
 * endpoint (numeroEleitoral frequently null, cpf only on the per-deputy detail call). */
@Component
class CamaraApiClient implements CamaraGateway {

  private static final Logger log = LoggerFactory.getLogger(CamaraApiClient.class);
  private static final int PAGE_SIZE = 100;

  private final RestClient restClient;

  CamaraApiClient(RestClient.Builder restClientBuilder, CamaraServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public List<CamaraParty> fetchAllParties() {
    List<CamaraParty> parties = new ArrayList<>();
    for (PartidoDto dto : fetchAllPages("/partidos", PartidosResponse.class, PartidosResponse::dados)) {
      PartidoDetailDto detail = fetchPartyDetail(dto.id());
      Integer number = detail == null ? null : detail.numeroEleitoral();
      String logoUrl = detail == null ? null : detail.urlLogo();
      parties.add(new CamaraParty(String.valueOf(dto.id()), dto.sigla(), dto.nome(), logoUrl, number));
    }
    return parties;
  }

  @Override
  public List<CamaraDeputy> fetchAllDeputies() {
    List<CamaraDeputy> deputies = new ArrayList<>();
    for (DeputadoDto dto : fetchAllPages("/deputados", DeputadosResponse.class, DeputadosResponse::dados)) {
      DeputadoDetailDto detail = fetchDeputyDetail(dto.id());
      String cpf = detail == null ? null : detail.cpf();
      String education = detail == null ? null : detail.escolaridade();
      List<String> socialLinks = detail == null || detail.redeSocial() == null ? List.of() : detail.redeSocial();
      UltimoStatusDto status = detail == null ? null : detail.ultimoStatus();
      GabineteDto gabinete = status == null ? null : status.gabinete();
      String phone = blankToNull(gabinete == null ? null : gabinete.telefone());
      String officeDetail = buildOfficeDetail(gabinete);
      String mandateStartDate = status == null ? null : status.data();
      deputies.add(
          new CamaraDeputy(
              String.valueOf(dto.id()),
              dto.nome(),
              dto.siglaPartido(),
              dto.siglaUf(),
              dto.urlFoto(),
              dto.email(),
              cpf,
              education,
              socialLinks,
              phone,
              officeDetail,
              mandateStartDate));
    }
    return deputies;
  }

  private PartidoDetailDto fetchPartyDetail(long id) {
    try {
      PartidoDetailEnvelope envelope = restClient.get().uri("/partidos/{id}", id).retrieve().body(PartidoDetailEnvelope.class);
      return envelope == null ? null : envelope.dados();
    } catch (RestClientException e) {
      log.warn("Câmara party detail lookup failed for id {}: {}", id, e.getMessage());
      return null;
    }
  }

  private DeputadoDetailDto fetchDeputyDetail(long id) {
    try {
      DeputadoDetailEnvelope envelope = restClient.get().uri("/deputados/{id}", id).retrieve().body(DeputadoDetailEnvelope.class);
      return envelope == null ? null : envelope.dados();
    } catch (RestClientException e) {
      log.warn("Câmara deputy detail lookup failed for id {}: {}", id, e.getMessage());
      return null;
    }
  }

  /** Builds a human-readable office location string (e.g. "Anexo IV, Sala 504, 5º andar") from
   * whatever gabinete fields Câmara actually populated — confirmed blank for out-of-mandate
   * deputies, so callers must tolerate a null/empty result. */
  private static String buildOfficeDetail(GabineteDto gabinete) {
    if (gabinete == null) {
      return null;
    }
    List<String> parts = new ArrayList<>();
    if (notBlank(gabinete.predio())) {
      parts.add("Anexo " + gabinete.predio());
    }
    if (notBlank(gabinete.sala())) {
      parts.add("Sala " + gabinete.sala());
    }
    if (notBlank(gabinete.andar())) {
      parts.add(gabinete.andar() + "º andar");
    }
    return parts.isEmpty() ? null : String.join(", ", parts);
  }

  private static boolean notBlank(String value) {
    return value != null && !value.isBlank();
  }

  private static String blankToNull(String value) {
    return notBlank(value) ? value : null;
  }

  private <T, R> List<T> fetchAllPages(String path, Class<R> responseType, Function<R, List<T>> itemsExtractor) {
    List<T> all = new ArrayList<>();
    int page = 1;
    while (true) {
      final int currentPage = page;
      R response =
          restClient
              .get()
              .uri(uriBuilder -> uriBuilder.path(path).queryParam("itens", PAGE_SIZE).queryParam("pagina", currentPage).build())
              .retrieve()
              .body(responseType);
      List<T> items = response == null ? List.of() : itemsExtractor.apply(response);
      if (items.isEmpty()) {
        break;
      }
      all.addAll(items);
      page++;
    }
    return all;
  }

  private record PartidosResponse(List<PartidoDto> dados) {}

  private record PartidoDto(long id, String sigla, String nome) {}

  private record PartidoDetailEnvelope(PartidoDetailDto dados) {}

  private record PartidoDetailDto(Integer numeroEleitoral, String urlLogo) {}

  private record DeputadosResponse(List<DeputadoDto> dados) {}

  private record DeputadoDto(long id, String nome, String siglaPartido, String siglaUf, String urlFoto, String email) {}

  private record DeputadoDetailEnvelope(DeputadoDetailDto dados) {}

  private record DeputadoDetailDto(String cpf, String escolaridade, List<String> redeSocial, UltimoStatusDto ultimoStatus) {}

  private record UltimoStatusDto(String data, GabineteDto gabinete) {}

  private record GabineteDto(String predio, String sala, String andar, String telefone) {}
}
