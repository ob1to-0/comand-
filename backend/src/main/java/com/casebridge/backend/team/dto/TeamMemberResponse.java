package com.casebridge.backend.team.dto;

import com.casebridge.backend.team.model.TeamRole;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TeamMemberResponse(
        UUID id,
        UUID userId,
        String userName,
        TeamRole role,
        OffsetDateTime joinedAt
) {}
