package com.casebridge.backend.team.repository;

import com.casebridge.backend.team.model.TeamMember;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findByTeamId(UUID teamId);

    List<TeamMember> findByTeamIdIn(Collection<UUID> teamIds);

    List<TeamMember> findByUserId(UUID userId);

    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId);

    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByTeamIdAndUserId(UUID teamId, UUID userId);
}
