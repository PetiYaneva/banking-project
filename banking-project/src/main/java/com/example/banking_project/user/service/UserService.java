package com.example.banking_project.user.service;

import com.example.banking_project.exception.UserAlreadyExistException;
import com.example.banking_project.security.UserDetailsImpl;
import com.example.banking_project.user.model.User;
import com.example.banking_project.user.model.UserRole;
import com.example.banking_project.user.repository.UserRepository;
import com.example.banking_project.user.validation.UserValidationService;
import com.example.banking_project.web.dto.RegisterRequest;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidationService userValidationService;

    public User registerUser(RegisterRequest registerRequest){
        userValidationService.validateUserRegister(registerRequest);

        User user = userRepository.save(initializeUser(registerRequest));

        log.info("Successfully create new user account for email [%s] and id [%s]".formatted(user.getEmail(), user.getId()));

        return user;
    }

    private User initializeUser(RegisterRequest registerRequest){
        return User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
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
}
