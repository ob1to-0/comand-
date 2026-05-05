package com.casebridge.backend.team.service;

import com.casebridge.backend.team.dto.CreateTeamRequest;
import com.casebridge.backend.team.dto.TeamMemberResponse;
import com.casebridge.backend.team.dto.TeamResponse;
import com.casebridge.backend.team.dto.UpdateTeamRequest;
import com.casebridge.backend.team.model.Team;
import com.casebridge.backend.team.model.TeamMember;
import com.casebridge.backend.team.model.TeamRole;
import com.casebridge.backend.team.repository.TeamMemberRepository;
import com.casebridge.backend.team.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamActivityService teamActivityService;

    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request, CurrentUser user) {
        assertJunior(user);
        ensureUserHasNoTeam(user.userId());

        Team team = new Team();
        team.setName(request.name());
        team.setDescription(request.description());
        team.setLeaderId(user.userId());
        team.setLeaderName(user.userName());
        Team saved = teamRepository.save(team);

        TeamMember leader = new TeamMember();
        leader.setTeam(saved);
        leader.setUserId(user.userId());
        leader.setUserName(user.userName());
        leader.setRole(TeamRole.LEADER);
        teamMemberRepository.save(leader);
        teamActivityService.record("TEAM_CREATED", saved.getName(), user.userName(), "Team created");

        return toResponse(saved, List.of(leader));
    }

    @Transactional(readOnly = true)
    public Page<TeamResponse> getTeams(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Team> teams = StringUtils.hasText(search)
                ? teamRepository.findByNameContainingIgnoreCase(search.trim(), pageable)
                : teamRepository.findAll(pageable);
        Map<UUID, List<TeamMember>> membersByTeam = loadMembersByTeam(teams.getContent());
        return teams.map(team -> toResponse(team, membersByTeam.getOrDefault(team.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getMyTeams(CurrentUser user) {
        assertJunior(user);
        List<TeamMember> memberships = teamMemberRepository.findByUserId(user.userId());
        List<Team> teams = memberships.stream().map(TeamMember::getTeam).toList();
        Map<UUID, List<TeamMember>> membersByTeam = loadMembersByTeam(teams);
        return teams.stream()
                .map(team -> toResponse(team, membersByTeam.getOrDefault(team.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeamById(UUID id) {
        Team team = getTeamOrThrow(id);
        return toResponse(team, teamMemberRepository.findByTeamId(team.getId()));
    }

    @Transactional
    public TeamResponse updateTeam(UUID teamId, UpdateTeamRequest request, CurrentUser user) {
        Team team = getTeamOrThrow(teamId);
        assertLeader(team, user);
        if (StringUtils.hasText(request.name())) {
            team.setName(request.name().trim());
        }
        if (request.description() != null) {
            team.setDescription(request.description());
        }
        Team updated = teamRepository.save(team);
        teamActivityService.record("TEAM_UPDATED", updated.getName(), user.userName(), "Team profile updated");
        return toResponse(updated, teamMemberRepository.findByTeamId(updated.getId()));
    }

    @Transactional
    public void deleteTeam(UUID teamId, CurrentUser user) {
        Team team = getTeamOrThrow(teamId);
        assertLeader(team, user);
        teamActivityService.record("TEAM_DELETED", team.getName(), user.userName(), "Team deleted by leader");
        teamRepository.delete(team);
    }

    @Transactional
    public void joinTeam(UUID teamId, CurrentUser user) {
        assertJunior(user);
        ensureUserHasNoTeam(user.userId());

        Team team = getTeamOrThrow(teamId);
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUserId(user.userId());
        member.setUserName(user.userName());
        member.setRole(TeamRole.MEMBER);
        teamMemberRepository.save(member);
        teamActivityService.record("MEMBER_JOINED", team.getName(), user.userName(), "Joined team");
    }

    @Transactional
    public void leaveTeam(UUID teamId, CurrentUser user) {
        TeamMember membership = teamMemberRepository.findByTeamIdAndUserId(teamId, user.userId())
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this team"));
        if (membership.getRole() == TeamRole.LEADER) {
            throw new IllegalArgumentException("Leader cannot leave team. Delete team or transfer ownership first");
        }
        teamMemberRepository.deleteByTeamIdAndUserId(teamId, user.userId());
        teamActivityService.record("MEMBER_LEFT", membership.getTeam().getName(), user.userName(), "Left team");
    }

    @Transactional
    public void removeMember(UUID teamId, UUID userId, CurrentUser user) {
        Team team = getTeamOrThrow(teamId);
        assertLeader(team, user);
        if (team.getLeaderId().equals(userId)) {
            throw new IllegalArgumentException("Leader cannot remove themselves");
        }
        TeamMember membership = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        teamMemberRepository.delete(membership);
        teamActivityService.record(
                "MEMBER_REMOVED",
                team.getName(),
                user.userName(),
                "Removed " + membership.getUserName()
        );
    }

    private Team getTeamOrThrow(UUID id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
    }

    private void assertJunior(CurrentUser user) {
        if (user.role() != CurrentUser.UserRole.JUNIOR) {
            throw new IllegalArgumentException("Only junior users can perform this action");
        }
    }

    private void assertLeader(Team team, CurrentUser user) {
        if (!team.getLeaderId().equals(user.userId())) {
            throw new IllegalArgumentException("Only team leader can perform this action");
        }
    }

    private void ensureUserHasNoTeam(UUID userId) {
        if (teamMemberRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("User can be a member of only one team at a time");
        }
    }

    private TeamResponse toResponse(Team team, List<TeamMember> teamMembers) {
        List<TeamMemberResponse> members = teamMembers.stream()
                .map(member -> new TeamMemberResponse(
                        member.getId(),
                        member.getUserId(),
                        member.getUserName(),
                        member.getRole(),
                        member.getJoinedAt()
                ))
                .collect(Collectors.toList());

        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getLeaderId(),
                team.getLeaderName(),
                members.size(),
                members,
                team.getCreatedAt(),
                team.getUpdatedAt()
        );
    }

    private Map<UUID, List<TeamMember>> loadMembersByTeam(List<Team> teams) {
        List<UUID> ids = teams.stream().map(Team::getId).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return teamMemberRepository.findByTeamIdIn(ids).stream()
                .collect(Collectors.groupingBy(member -> member.getTeam().getId()));
    }
}
