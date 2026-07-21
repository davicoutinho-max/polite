package dev.civicpulse.directory.adapter.in.messaging;

import dev.civicpulse.directory.adapter.in.messaging.dto.AccountRegisteredMessage;
import dev.civicpulse.directory.adapter.in.messaging.dto.PartyRegisteredMessage;
import dev.civicpulse.directory.adapter.in.messaging.dto.PoliticianReassignedMessage;
import dev.civicpulse.directory.adapter.in.messaging.dto.PoliticianRegisteredMessage;
import dev.civicpulse.directory.adapter.in.messaging.dto.RepresentativeLinkedMessage;
import dev.civicpulse.directory.application.port.in.ProjectDirectoryUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Translates the events listed in docs/db/directory-service/schema.sql's "Domain events
 * consumed" section into {@link ProjectDirectoryUseCase} calls. This is the only place in the
 * service that knows about Kafka topic names/payload shapes. */
@Component
class DirectoryProjectionListener {

  private final ProjectDirectoryUseCase projectDirectoryUseCase;

  DirectoryProjectionListener(ProjectDirectoryUseCase projectDirectoryUseCase) {
    this.projectDirectoryUseCase = projectDirectoryUseCase;
  }

  @KafkaListener(topics = "account-registered", groupId = "directory-service")
  void onAccountRegistered(AccountRegisteredMessage message) {
    projectDirectoryUseCase.onAccountRegistered(message.accountId(), message.accountType());
  }

  @KafkaListener(topics = "representative-linked", groupId = "directory-service")
  void onRepresentativeLinked(RepresentativeLinkedMessage message) {
    projectDirectoryUseCase.onRepresentativeLinked(
        message.politicianAccountId(), message.partyId(), message.roleTitle(), message.state(), message.occurredAt());
  }

  @KafkaListener(topics = "politician-registered", groupId = "directory-service")
  void onPoliticianRegistered(PoliticianRegisteredMessage message) {
    projectDirectoryUseCase.onPoliticianRegistered(message.politicianAccountId(), message.partyId(), message.occurredAt());
  }

  @KafkaListener(topics = "politician-reassigned", groupId = "directory-service")
  void onPoliticianReassigned(PoliticianReassignedMessage message) {
    projectDirectoryUseCase.onPoliticianReassigned(message.politicianAccountId(), message.partyId(), message.occurredAt());
  }

  @KafkaListener(topics = "party-registered", groupId = "directory-service")
  void onPartyRegistered(PartyRegisteredMessage message) {
    projectDirectoryUseCase.onPartyRegistered(
        message.partyId(), message.name(), message.acronym(), message.number(), message.president(), message.ideology(), message.occurredAt());
  }
}
