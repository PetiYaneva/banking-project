package com.example.banking_project.security;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.user.model.User;
import com.example.banking_project.user.service.UserService;
import com.example.banking_project.user.validation.UserValidationService;
import com.example.banking_project.web.dto.AuthenticationRequest;
import com.example.banking_project.web.dto.AuthenticationResponse;
import com.example.banking_project.web.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserValidationService userValidationService;


    public AuthenticationResponse register(RegisterRequest registerRequest) {
        User user = userService.register(registerRequest);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );

        var jwt = jwtService.generateToken(buildClaims(Optional.of(user)), userDetails);

        return AuthenticationResponse.builder()
                .token(jwt)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        userValidationService.validateUserLogin(authenticationRequest);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()
                )
        );

        Optional<User> user = userService.getUserByEmail(authenticationRequest.getEmail());

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.get().getEmail(),
                user.get().getPassword(),
                List.of(new SimpleGrantedAuthority(user.get().getRole().name()))
        );

        var jwtToken = jwtService.generateToken(buildClaims(user), userDetails);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    private Map<String, Object> buildClaims(Optional<User> user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.get().getId().toString());
        claims.put("role", user.get().getRole().name());

        List<Account> accounts = user.get().getAccounts();
        if (accounts != null && !accounts.isEmpty()) {
            List<String> ibans = accounts.stream()
                    .map(Account::getIban)
                    .toList();
            claims.put("ibans", ibans); // добавя масив от IBAN-и
        }

        return claims;
    }

}
