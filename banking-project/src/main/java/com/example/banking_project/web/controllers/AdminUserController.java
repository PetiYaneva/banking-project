package com.example.banking_project.web.controllers.admin;

import com.example.banking_project.user.model.User;
import com.example.banking_project.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsersOrdered() {
        return ResponseEntity.ok(userService.getAllUsersOrderedByCreatedAtDesc());
    }

    @GetMapping("/period")
    public ResponseEntity<List<User>> getUsersByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(userService.getUsersByPeriod(from, to));
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchByEmail(@RequestParam("email") String emailPart) {
        return ResponseEntity.ok(userService.searchUsersByEmail(emailPart));
    }

    @GetMapping("/uncompleted")
    public ResponseEntity<List<User>> getUncompletedProfiles() {
        return ResponseEntity.ok(userService.getUsersWithUncompletedProfile());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }
}
