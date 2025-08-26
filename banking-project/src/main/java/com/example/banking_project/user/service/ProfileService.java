package com.example.banking_project.user.service;

import com.example.banking_project.user.model.User;
import com.example.banking_project.user.repository.UserRepository;
import com.example.banking_project.web.dto.CompleteProfileRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;

    @Transactional
    public void completeProfileByEmail(String email, CompleteProfileRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setAddress(req.getAddress());
        user.setEmployment(req.getEmployment());
        user.setDeclaredIncome(req.getDeclaredIncome());
        user.setProfileCompleted(true);

        userRepository.save(user);
    }

    @Transactional
    public boolean isProfileCompleted(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return Boolean.TRUE.equals(user.isProfileCompleted());
    }

}
