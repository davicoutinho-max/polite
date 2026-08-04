package dev.civicpulse.legislative.adapter.in.web.dto;

import jakarta.validation.constraints.PositiveOrZero;

public record UpdateActivityCountsRequest(
    @PositiveOrZero int speechesCount, @PositiveOrZero int interviewsCount, @PositiveOrZero int tripsCount) {}
