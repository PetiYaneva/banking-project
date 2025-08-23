package com.example.banking_project.web.controllers;

import com.example.banking_project.user.service.ProfileService;
import com.example.banking_project.web.dto.CompleteProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/complete")
    public ResponseEntity<Void> completeProfile(@AuthenticationPrincipal UserDetails principal,
                                                @Valid @RequestBody CompleteProfileRequest request) {
        profileService.completeProfileByEmail(principal.getUsername(), request);
        return ResponseEntity.noContent().build();
    }
}
