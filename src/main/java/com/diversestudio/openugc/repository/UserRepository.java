package com.diversestudio.openugc.repository;

import com.diversestudio.openugc.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // For authentication lookup

}
