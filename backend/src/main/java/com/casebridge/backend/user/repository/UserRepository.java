package com.casebridge.backend.user.repository;

import com.casebridge.backend.user.model.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
}
