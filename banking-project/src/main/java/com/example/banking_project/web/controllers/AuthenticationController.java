package com.example.banking_project.web.controllers;

import com.example.banking_project.security.AuthenticationService;
import com.example.banking_project.web.dto.AuthenticationRequest;
import com.example.banking_project.web.dto.AuthenticationResponse;
import com.example.banking_project.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api", produces = "application/json")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping(value = "/register", consumes = "application/json")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        var resp = authenticationService.register(request);
        return ResponseEntity.status(201).body(resp);
    }

    @PostMapping(value = "/login", consumes = "application/json")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        var resp = authenticationService.authenticate(request);
        return ResponseEntity.ok(resp);
    }
}
