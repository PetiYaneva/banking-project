package com.example.banking_project.user.validation;

import com.example.banking_project.web.dto.AuthenticationRequest;
import com.example.banking_project.web.dto.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public interface UserValidationService {
    void validateUserRegister(RegisterRequest request);
    void validateUserLogin(AuthenticationRequest request);
}
