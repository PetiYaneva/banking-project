package com.example.banking_project.user.service;

import com.example.banking_project.security.UserDetailsImpl;
import com.example.banking_project.user.model.User;
import com.example.banking_project.user.model.UserRole;
import com.example.banking_project.user.repository.UserRepository;
import com.example.banking_project.user.validation.UserValidationService;
import com.example.banking_project.web.dto.RegisterRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidationService userValidationService;

    public User register(RegisterRequest registerRequest){
        userValidationService.validateUserRegister(registerRequest);

        User user = userRepository.save(initializeUser(registerRequest));

        log.info("Successfully create new user account for email [%s] and id [%s]".formatted(user.getEmail(), user.getId()));

        return user;
    }

    public Boolean isProfileCompletedByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return !user.isEmpty() ? user.get().isProfileCompleted() : false;
    }

    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private User initializeUser(RegisterRequest registerRequest){
        return User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .createdOn(LocalDate.now())
                .updatedOn(LocalDate.now())
                .build();
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not Found with username: " + email)
                );

        return UserDetailsImpl.build(user);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers (){
        return List.of((User) userRepository.findAll());
    }

    public List<User> getAllUsersOrderedByCreatedAtDesc() {
        return userRepository.getAllByOrderByCreatedOnDesc();
    }


    public List<User> getUsersByPeriod(LocalDateTime from, LocalDateTime to) {
        return userRepository.findAllByCreatedOnBetween(from, to);
    }

    public List<User> searchUsersByEmail(String queryPart) {
        return userRepository.findAllByEmailContainingIgnoreCase(queryPart);
    }

    public List<User> getUsersWithUncompletedProfile() {
        return userRepository.findAllByProfileCompletedFalse();
    }
}
