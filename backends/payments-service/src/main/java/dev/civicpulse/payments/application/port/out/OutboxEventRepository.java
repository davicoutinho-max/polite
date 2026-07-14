package dev.civicpulse.payments.application.port.out;

import dev.civicpulse.payments.domain.model.OutboxEvent;
import java.util.List;

public interface OutboxEventRepository {

  OutboxEvent save(OutboxEvent event);

  /** Oldest-first, capped to {@code limit} — see {@code idx_outbox_unpublished}, sized for
   * exactly this scan. */
  List<OutboxEvent> findUnpublished(int limit);
}
