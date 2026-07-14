package dev.civicpulse.legislative.adapter.in.messaging;

import dev.civicpulse.legislative.adapter.in.messaging.dto.PoliticianRegisteredMessage;
import dev.civicpulse.legislative.adapter.in.messaging.dto.RepresentativeLinkedMessage;
import dev.civicpulse.legislative.application.DossierProjectionUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Translates the events listed in docs/db/legislative-service/schema.sql's "Domain events
 * consumed" section into {@link DossierProjectionUseCase} calls. This is the only place in the
 * service that knows about Kafka topic names/payload shapes. */
@Component
class LegislativeProjectionListener {

  private final DossierProjectionUseCase dossierProjectionUseCase;

  LegislativeProjectionListener(DossierProjectionUseCase dossierProjectionUseCase) {
    this.dossierProjectionUseCase = dossierProjectionUseCase;
  }

  @KafkaListener(topics = "politician-registered", groupId = "legislative-service")
  void onPoliticianRegistered(PoliticianRegisteredMessage message) {
    dossierProjectionUseCase.ensureDossierExists(message.politicianAccountId());
  }

  @KafkaListener(topics = "representative-linked", groupId = "legislative-service")
  void onRepresentativeLinked(RepresentativeLinkedMessage message) {
    dossierProjectionUseCase.ensureDossierExists(message.politicianAccountId());
  }
}
