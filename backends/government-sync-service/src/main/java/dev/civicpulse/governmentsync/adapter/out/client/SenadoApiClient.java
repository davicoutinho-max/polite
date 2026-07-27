package dev.civicpulse.governmentsync.adapter.out.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.civicpulse.governmentsync.application.port.out.SenadoGateway;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** legis.senado.leg.br/dadosabertos — the response payload preserves the underlying XML dataset's
 * PascalCase field names even in its JSON form, hence the {@code @JsonProperty} mapping below. */
@Component
class SenadoApiClient implements SenadoGateway {

  private final RestClient restClient;

  SenadoApiClient(RestClient.Builder restClientBuilder, SenadoServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public List<SenadoSenator> fetchCurrentSenators() {
    SenadoAtualResponse response =
        restClient.get().uri("/senador/lista/atual").accept(MediaType.APPLICATION_JSON).retrieve().body(SenadoAtualResponse.class);
    if (response == null || response.lista() == null || response.lista().parlamentares() == null) {
      return List.of();
    }
    return response.lista().parlamentares().parlamentar().stream()
        .map(
            p -> {
              IdentificacaoParlamentarDto id = p.identificacao();
              List<TelefoneDto> telefones = id.telefones() == null ? null : id.telefones().telefone();
              String phone = telefones == null || telefones.isEmpty() ? null : telefones.get(0).numero();
              LegislaturaDto primeiraLegislatura = p.mandato() == null ? null : p.mandato().primeiraLegislatura();
              String mandateStartDate = primeiraLegislatura == null ? null : primeiraLegislatura.dataInicio();
              return new SenadoSenator(
                  id.codigoParlamentar(),
                  id.nomeParlamentar(),
                  id.siglaPartidoParlamentar(),
                  id.ufParlamentar(),
                  id.urlFotoParlamentar(),
                  id.emailParlamentar(),
                  phone,
                  mandateStartDate);
            })
        .toList();
  }

  private record SenadoAtualResponse(@JsonProperty("ListaParlamentarEmExercicio") ListaParlamentarEmExercicio lista) {}

  private record ListaParlamentarEmExercicio(@JsonProperty("Parlamentares") Parlamentares parlamentares) {}

  private record Parlamentares(@JsonProperty("Parlamentar") List<ParlamentarDto> parlamentar) {}

  private record ParlamentarDto(
      @JsonProperty("IdentificacaoParlamentar") IdentificacaoParlamentarDto identificacao,
      @JsonProperty("Mandato") MandatoDto mandato) {}

  private record IdentificacaoParlamentarDto(
      @JsonProperty("CodigoParlamentar") String codigoParlamentar,
      @JsonProperty("NomeParlamentar") String nomeParlamentar,
      @JsonProperty("UrlFotoParlamentar") String urlFotoParlamentar,
      @JsonProperty("EmailParlamentar") String emailParlamentar,
      @JsonProperty("SiglaPartidoParlamentar") String siglaPartidoParlamentar,
      @JsonProperty("UfParlamentar") String ufParlamentar,
      @JsonProperty("Telefones") TelefonesDto telefones) {}

  private record TelefonesDto(@JsonProperty("Telefone") List<TelefoneDto> telefone) {}

  private record TelefoneDto(@JsonProperty("NumeroTelefone") String numero) {}

  private record MandatoDto(@JsonProperty("PrimeiraLegislaturaDoMandato") LegislaturaDto primeiraLegislatura) {}

  private record LegislaturaDto(@JsonProperty("DataInicio") String dataInicio) {}
}
