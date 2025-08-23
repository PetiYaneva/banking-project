package com.example.banking_project.security;

import com.example.banking_project.user.model.User;
import com.example.banking_project.user.model.UserRole;
import com.example.banking_project.user.repository.UserRepository;
import com.example.banking_project.user.validation.UserValidationService;
import com.example.banking_project.web.dto.AuthenticationRequest;
import com.example.banking_project.web.dto.AuthenticationResponse;
import com.example.banking_project.web.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserValidationService userValidationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        userValidationService.validateUserRegister(request);

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .profileCompleted(false)
                .build();

        userRepository.save(user);

        UserDetails principal = UserDetailsImpl.build(user);
        String token = jwtService.generateToken(buildClaims(user), principal);

        return AuthenticationResponse.builder().token(token).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails principal = UserDetailsImpl.build(user);
        String token = jwtService.generateToken(buildClaims(user), principal);

        return AuthenticationResponse.builder().token(token).build();
    }

    private Map<String, Object> buildClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("role", user.getRole().name());
        claims.put("profileCompleted", user.isProfileCompleted());
        return claims;
    }
}
