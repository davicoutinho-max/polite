package dev.civicpulse.platformconfig.adapter.in.web.dto;

import dev.civicpulse.platformconfig.domain.model.Country;
import java.util.UUID;

public record CountryResponse(UUID id, String name, String code) {

  public static CountryResponse from(Country country) {
    return new CountryResponse(country.id(), country.name(), country.code());
  }
}
