package dev.civicpulse.feedcontent.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PublishAgendaPostRequest(
    @NotBlank String title,
    @NotBlank String eventDate,
    @NotBlank String location,
    String imageUrl,
    String fileUrl,
    String fileName,
    List<String> pollOptions,
    String visibility,
    String context) {}
