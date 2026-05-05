package com.casebridge.backend.team.dto;

import jakarta.validation.constraints.Size;

public record UpdateTeamRequest(
        @Size(max = 100) String name,
        @Size(max = 1000) String description
) {}
