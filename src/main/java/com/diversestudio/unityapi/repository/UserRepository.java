package com.diversestudio.unityapi.repository;

import com.diversestudio.unityapi.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // For authentication lookup
}
