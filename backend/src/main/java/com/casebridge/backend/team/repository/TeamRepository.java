package com.casebridge.backend.team.repository;

import com.casebridge.backend.team.model.Team;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    Page<Team> findByNameContainingIgnoreCase(String search, Pageable pageable);

    List<Team> findByLeaderId(UUID leaderId);
}
