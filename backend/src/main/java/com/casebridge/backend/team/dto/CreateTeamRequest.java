package com.casebridge.backend.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeamRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 1000) String description
) {}
