package com.casebridge.backend.team.service;

import java.util.UUID;

public record CurrentUser(UUID userId, String userName, UserRole role) {
    public enum UserRole {
        JUNIOR,
        COMPANY
    }
}
