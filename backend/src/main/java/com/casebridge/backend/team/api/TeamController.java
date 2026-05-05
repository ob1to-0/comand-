package com.casebridge.backend.team.api;

import com.casebridge.backend.team.dto.CreateTeamRequest;
import com.casebridge.backend.team.dto.TeamActivityResponse;
import com.casebridge.backend.team.dto.TeamResponse;
import com.casebridge.backend.team.dto.UpdateTeamRequest;
import com.casebridge.backend.team.service.CurrentUser;
import com.casebridge.backend.team.service.TeamActivityService;
import com.casebridge.backend.team.service.TeamService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;
    private final TeamActivityService teamActivityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse createTeam(
            @Valid @RequestBody CreateTeamRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName,
            @RequestHeader("X-User-Role") String userRole
    ) {
        return teamService.createTeam(request, buildUser(userId, userName, userRole));
    }

    @GetMapping
    public Page<TeamResponse> getTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return teamService.getTeams(search, page, size);
    }

    @GetMapping("/my")
    public List<TeamResponse> getMyTeams(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName,
            @RequestHeader("X-User-Role") String userRole
    ) {
        return teamService.getMyTeams(buildUser(userId, userName, userRole));
    }

    @GetMapping("/{id}")
    public TeamResponse getTeam(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName,
            @RequestHeader("X-User-Role") String userRole
    ) {
        buildUser(userId, userName, userRole);
        return teamService.getTeamById(id);
    }

    @PutMapping("/{id}")
    public TeamResponse updateTeam(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTeamRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName,
            @RequestHeader("X-User-Role") String userRole
    ) {
        return teamService.updateTeam(id, request, buildUser(userId, userName, userRole));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeam(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName,
            @RequestHeader("X-User-Role") String userRole
    ) {
        teamService.deleteTeam(id, buildUser(userId, userName, userRole));
    }

    @PostMapping("/{teamId}/join")
    public ResponseEntity<Void> joinTeam(
            @PathVariable UUID teamId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName,
            @RequestHeader("X-User-Role") String userRole
    ) {
        teamService.joinTeam(teamId, buildUser(userId, userName, userRole));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{teamId}/leave")
    public ResponseEntity<Void> leaveTeam(
            @PathVariable UUID teamId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName,
            @RequestHeader("X-User-Role") String userRole
    ) {
        teamService.leaveTeam(teamId, buildUser(userId, userName, userRole));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID teamId,
            @PathVariable UUID userId,
            @RequestHeader("X-User-Id") UUID actorId,
            @RequestHeader("X-User-Name") String userName,
            @RequestHeader("X-User-Role") String userRole
    ) {
        teamService.removeMember(teamId, userId, buildUser(actorId, userName, userRole));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/activity")
    public List<TeamActivityResponse> getRecentActivity(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName,
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam(defaultValue = "30") int limit
    ) {
        buildUser(userId, userName, userRole);
        return teamActivityService.getRecent(limit);
    }

    private CurrentUser buildUser(UUID userId, String userName, String userRole) {
        return new CurrentUser(
                userId,
                userName,
                CurrentUser.UserRole.valueOf(userRole.trim().toUpperCase())
        );
    }
}
