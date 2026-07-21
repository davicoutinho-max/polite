package dev.civicpulse.messaging.application.port.in;

import dev.civicpulse.messaging.domain.model.Message;
import java.util.UUID;

public interface EditMessageUseCase {

  /** Only the original sender may edit their own message — see MessageService. */
  Message edit(UUID messageId, UUID requesterAccountId, String newBody);

  /** Only the original sender may delete their own message — see MessageService. */
  Message delete(UUID messageId, UUID requesterAccountId);
}
