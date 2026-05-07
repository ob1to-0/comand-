package com.casebridge.backend.user.service;

import com.casebridge.backend.team.model.TeamMember;
import com.casebridge.backend.team.model.TeamRole;
import com.casebridge.backend.team.repository.TeamMemberRepository;
import com.casebridge.backend.team.repository.TeamRepository;
import com.casebridge.backend.team.service.CurrentUser;
import com.casebridge.backend.team.service.TeamActivityService;
import com.casebridge.backend.user.model.User;
import com.casebridge.backend.user.model.UserRole;
import com.casebridge.backend.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamActivityService teamActivityService;

    @Transactional
    public void sync(CurrentUser currentUser) {
        User user = userRepository.findById(currentUser.userId()).orElseGet(User::new);
        user.setId(currentUser.userId());
        user.setName(currentUser.userName());
        user.setRole(UserRole.valueOf(currentUser.role().name()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<TeamMember> memberships = teamMemberRepository.findByUserId(userId);
        for (TeamMember membership : memberships) {
            if (membership.getRole() == TeamRole.LEADER) {
                teamActivityService.record(
                        "TEAM_DELETED",
                        membership.getTeam().getName(),
                        user.getName(),
                        "Team deleted due to leader account removal"
                );
                teamRepository.delete(membership.getTeam());
            } else {
                teamActivityService.record(
                        "MEMBER_REMOVED",
                        membership.getTeam().getName(),
                        user.getName(),
                        "User account deleted"
                );
                teamMemberRepository.delete(membership);
            }
        }
        userRepository.delete(user);
    }
}
