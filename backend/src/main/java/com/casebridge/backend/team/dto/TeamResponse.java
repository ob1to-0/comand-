package com.casebridge.backend.team.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        String description,
        UUID leaderId,
        String leaderName,
        int memberCount,
        List<TeamMemberResponse> members,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
