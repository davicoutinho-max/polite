package dev.civicpulse.governmentsync.adapter.out.client;

import dev.civicpulse.governmentsync.application.port.out.TseElectionDataGateway;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** dadosabertos.tse.jus.br's bulk CDN files — confirmed via direct download+inspection (see
 * TseElectionDataGateway's javadoc) that {@code votacao_candidato_munzona_<year>.zip} bundles
 * every UF's CSV as separate zip entries (tens to hundreds of MB uncompressed for the larger
 * states/years). Both the download and the unzip are streamed — the target UF's entry is parsed
 * line-by-line straight out of the zip's DeflaterInputStream, so memory use stays bounded by one
 * CSV line, not by file size, even for São Paulo's ~70MB entry. */
@Component
class TseElectionDataClient implements TseElectionDataGateway {

  private static final Logger log = LoggerFactory.getLogger(TseElectionDataClient.class);

  // CD_SIT_TOT_TURNO values confirmed via a live sample (2022 Sergipe/DF, 2024 Sergipe):
  // 1=ELEITO, 2=ELEITO POR QP, 3=ELEITO POR MÉDIA, 4=NÃO ELEITO, 5=SUPLENTE.
  private static final Set<String> ELECTED_CODES = Set.of("1", "2", "3");

  // Fixed column indices from the confirmed header (DT_GERACAO ... DS_SIT_TOT_TURNO, 50 columns) —
  // stable across the 2022 and 2024 downloads inspected, both under the same TSE dataset.
  private static final int COL_SG_UF = 10;
  private static final int COL_NM_MUNICIPIO = 14;
  private static final int COL_DS_CARGO = 17;
  private static final int COL_SQ_CANDIDATO = 18;
  private static final int COL_NM_CANDIDATO = 20;
  private static final int COL_NM_URNA_CANDIDATO = 21;
  private static final int COL_NR_TURNO = 5;
  private static final int COL_NR_PARTIDO = 34;
  private static final int COL_SG_PARTIDO = 35;
  private static final int COL_NM_PARTIDO = 36;
  private static final int COL_QT_VOTOS_NOMINAIS = 45;
  private static final int COL_CD_SIT_TOT_TURNO = 48;
  private static final int MIN_COLUMNS = 50;

  private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
  private final TseServiceProperties properties;

  TseElectionDataClient(TseServiceProperties properties) {
    this.properties = properties;
  }

  @Override
  public List<TseElectedCandidate> fetchElectedCandidates(int year, String uf, Set<String> cargoFilter) {
    return downloadAndParse(year, uf, zis -> parseElected(zis, cargoFilter));
  }

  @Override
  public List<TseElectionResult> fetchElectionResults(int year, String uf, Set<String> cargoFilter, boolean groupByMunicipality) {
    return downloadAndParse(year, uf, zis -> parseAllWithVotes(zis, cargoFilter, groupByMunicipality));
  }

  private <T> List<T> downloadAndParse(int year, String uf, ParsingFunction<T> parser) {
    String url = properties.baseUrl() + "/votacao_candidato_munzona_" + year + ".zip";
    String targetEntryName = "votacao_candidato_munzona_" + year + "_" + uf.toUpperCase() + ".csv";

    HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
    try {
      HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
      if (response.statusCode() != 200) {
        log.warn("TSE dataset download returned HTTP {} for {}", response.statusCode(), url);
        return List.of();
      }
      try (ZipInputStream zis = new ZipInputStream(response.body())) {
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
          if (entry.getName().equals(targetEntryName)) {
            return parser.parse(zis);
          }
        }
      }
      log.warn("TSE dataset {} had no entry named {}", url, targetEntryName);
      return List.of();
    } catch (IOException | InterruptedException e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      log.warn("Failed to download/parse TSE dataset {} for UF {}: {}", url, uf, e.getMessage());
      return List.of();
    }
  }

  @FunctionalInterface
  private interface ParsingFunction<T> {
    List<T> parse(InputStream entryStream) throws IOException;
  }

  /** One row per (candidate, município, zona) — the same {@code SQ_CANDIDATO} repeats across
   * every município/zona it received votes in, with an identical elected/not-elected verdict on
   * every occurrence (the verdict is computed once for the whole race, not per município). This
   * dedupes by candidate, keeping the map-insertion-order streaming behavior bounded to one CSV
   * line read at a time rather than materializing the raw file. */
  private List<TseElectedCandidate> parseElected(java.io.InputStream entryStream, Set<String> cargoFilter) throws IOException {
    Map<String, TseElectedCandidate> byCandidate = new LinkedHashMap<>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(entryStream, StandardCharsets.ISO_8859_1));
    reader.readLine(); // header
    String line;
    while ((line = reader.readLine()) != null) {
      String[] fields = splitCsvLine(line);
      if (fields.length < MIN_COLUMNS) {
        continue;
      }
      String cargo = fields[COL_DS_CARGO];
      if (!cargoFilter.contains(cargo)) {
        continue;
      }
      if (!ELECTED_CODES.contains(fields[COL_CD_SIT_TOT_TURNO])) {
        continue;
      }
      String externalId = fields[COL_SQ_CANDIDATO];
      byCandidate.put(
          externalId,
          new TseElectedCandidate(
              externalId,
              fields[COL_NM_CANDIDATO],
              fields[COL_NM_URNA_CANDIDATO],
              cargo,
              fields[COL_SG_PARTIDO],
              parseIntOrNull(fields[COL_NR_PARTIDO]),
              fields[COL_NM_PARTIDO],
              fields[COL_SG_UF],
              fields[COL_NM_MUNICIPIO]));
    }
    return List.copyOf(byCandidate.values());
  }

  /** Every candidate for the given cargos, votes summed across every (município, zona) row the
   * same {@code SQ_CANDIDATO} appears in, then ranked within its race (see {@code
   * groupByMunicipality}'s javadoc on the gateway interface for what defines "a race" here). */
  private List<TseElectionResult> parseAllWithVotes(InputStream entryStream, Set<String> cargoFilter, boolean groupByMunicipality)
      throws IOException {
    Map<String, Accumulator> byCandidate = new LinkedHashMap<>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(entryStream, StandardCharsets.ISO_8859_1));
    reader.readLine(); // header
    String line;
    while ((line = reader.readLine()) != null) {
      String[] fields = splitCsvLine(line);
      if (fields.length < MIN_COLUMNS) {
        continue;
      }
      String cargo = fields[COL_DS_CARGO];
      if (!cargoFilter.contains(cargo)) {
        continue;
      }
      String externalId = fields[COL_SQ_CANDIDATO];
      Accumulator acc =
          byCandidate.computeIfAbsent(
              externalId,
              id ->
                  new Accumulator(
                      id,
                      fields[COL_NM_CANDIDATO],
                      fields[COL_NM_URNA_CANDIDATO],
                      cargo,
                      fields[COL_SG_PARTIDO],
                      parseIntOrNull(fields[COL_NR_PARTIDO]),
                      fields[COL_NM_PARTIDO],
                      fields[COL_SG_UF],
                      fields[COL_NM_MUNICIPIO]));
      Integer parsedRound = parseIntOrNull(fields[COL_NR_TURNO]);
      int round = parsedRound == null ? 1 : parsedRound;
      acc.votesByRound.merge(round, parseLongOr0(fields[COL_QT_VOTOS_NOMINAIS]), Long::sum);
      acc.elected = acc.elected || ELECTED_CODES.contains(fields[COL_CD_SIT_TOT_TURNO]);
    }

    Map<String, List<Accumulator>> byRace = new LinkedHashMap<>();
    for (Accumulator acc : byCandidate.values()) {
      String raceKey = groupByMunicipality ? acc.cargo + "|" + acc.municipality : acc.cargo;
      byRace.computeIfAbsent(raceKey, k -> new ArrayList<>()).add(acc);
    }

    List<TseElectionResult> results = new ArrayList<>();
    for (List<Accumulator> race : byRace.values()) {
      // Governador (and, nationally, Presidente) can go to a second-round runoff — TSE keeps
      // separate rows per NR_TURNO for the same candidate, so summing votes across rounds would
      // conflate two different elections and scramble the ranking (confirmed against the 2022 SE
      // Governador race: naively summed votes ranked the round-1 leader who LOST the runoff ahead
      // of the actual winner). Each candidate's own highest round is their "final" tally; a
      // candidate who reached the runoff always outranks one who didn't, and — within either
      // group — more votes in that final round ranks higher.
      race.sort(
          Comparator.comparingInt((Accumulator a) -> a.highestRound())
              .thenComparingLong(Accumulator::votesInHighestRound)
              .reversed());
      for (int i = 0; i < race.size(); i++) {
        Accumulator acc = race.get(i);
        results.add(
            new TseElectionResult(
                acc.externalId,
                acc.legalName,
                acc.ballotName,
                acc.cargo,
                acc.partyAcronym,
                acc.partyNumber,
                acc.partyName,
                acc.uf,
                acc.municipality,
                acc.votesInHighestRound(),
                i + 1,
                acc.elected));
      }
    }
    return results;
  }

  /** Mutable accumulator for summing votes-per-round across a candidate's repeated CSV rows —
   * {@link TseElectionResult} itself stays an immutable record since rank isn't known until every
   * row has been read and the whole race can be sorted. */
  private static final class Accumulator {
    final String externalId;
    final String legalName;
    final String ballotName;
    final String cargo;
    final String partyAcronym;
    final Integer partyNumber;
    final String partyName;
    final String uf;
    final String municipality;
    final Map<Integer, Long> votesByRound = new HashMap<>();
    boolean elected;

    int highestRound() {
      return votesByRound.keySet().stream().mapToInt(Integer::intValue).max().orElse(1);
    }

    long votesInHighestRound() {
      return votesByRound.getOrDefault(highestRound(), 0L);
    }

    Accumulator(
        String externalId,
        String legalName,
        String ballotName,
        String cargo,
        String partyAcronym,
        Integer partyNumber,
        String partyName,
        String uf,
        String municipality) {
      this.externalId = externalId;
      this.legalName = legalName;
      this.ballotName = ballotName;
      this.cargo = cargo;
      this.partyAcronym = partyAcronym;
      this.partyNumber = partyNumber;
      this.partyName = partyName;
      this.uf = uf;
      this.municipality = municipality;
    }
  }

  private static long parseLongOr0(String value) {
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException | NullPointerException e) {
      return 0L;
    }
  }

  /** Fields carry no embedded semicolons/quotes in this dataset (confirmed on the samples
   * inspected) — a plain split plus quote-stripping is enough, no CSV escaping to handle. */
  private static String[] splitCsvLine(String line) {
    String[] raw = line.split(";", -1);
    String[] result = new String[raw.length];
    for (int i = 0; i < raw.length; i++) {
      String v = raw[i];
      if (v.length() >= 2 && v.charAt(0) == '"' && v.charAt(v.length() - 1) == '"') {
        v = v.substring(1, v.length() - 1);
      }
      result[i] = v;
    }
    return result;
  }

  private static Integer parseIntOrNull(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
