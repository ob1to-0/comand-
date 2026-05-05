package com.casebridge.backend.team.dto;

import java.time.OffsetDateTime;

public record TeamActivityResponse(
        OffsetDateTime timestamp,
        String action,
        String teamName,
        String actorName,
        String details
) {}
