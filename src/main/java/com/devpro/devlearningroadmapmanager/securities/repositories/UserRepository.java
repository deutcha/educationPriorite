package com.devpro.devlearningroadmapmanager.securities.repositories;

import com.devpro.devlearningroadmapmanager.securities.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    User findByEmail(String email);
    User findByUsername(String username);
}
