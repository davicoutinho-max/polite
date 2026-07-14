package dev.civicpulse.legislative.application.port.in;

import dev.civicpulse.legislative.application.TransparencyView;
import dev.civicpulse.legislative.domain.model.TransparencyMetric;
import java.util.UUID;

public interface TransparencyUseCase {

  TransparencyView getTransparency(UUID politicianAccountId);

  TransparencyMetric addMetric(UUID politicianAccountId, String icon, String label, long valueCents, String caption, String period);

  void addExpenseLine(UUID politicianAccountId, String category, long amountCents);
}
