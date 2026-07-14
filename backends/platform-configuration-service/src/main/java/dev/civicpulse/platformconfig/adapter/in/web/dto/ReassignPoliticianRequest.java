package dev.civicpulse.platformconfig.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReassignPoliticianRequest(@NotNull UUID partyId) {}
