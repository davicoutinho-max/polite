package dev.civicpulse.feedcontent.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record PublishAgendaPostRequest(
    @NotBlank String title, @NotBlank String eventDate, @NotBlank String location, String visibility, String context) {}
