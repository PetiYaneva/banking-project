package com.example.banking_project.user.repository;

import com.example.banking_project.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    List<User> getAllByOrderByCreatedOnDesc();

    List<User> findAllByCreatedOnBetween(LocalDateTime from, LocalDateTime to);
    List<User> findAll();
    List<User> findAllByEmailContainingIgnoreCase(String part);
    List<User> findAllByProfileCompletedFalse();
}
