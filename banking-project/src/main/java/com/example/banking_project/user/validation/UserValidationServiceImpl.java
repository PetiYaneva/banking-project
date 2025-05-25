package com.example.banking_project.user.validation;

import com.example.banking_project.exception.UserAlreadyExistException;
import com.example.banking_project.user.repository.UserRepository;
import com.example.banking_project.web.dto.AuthenticationRequest;
import com.example.banking_project.web.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserValidationServiceImpl implements UserValidationService {
    private final UserRepository userRepository;

    @Override
    public void validateUserRegister(RegisterRequest request) {
        // 1. Имейл вече съществува
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistException(
                    "User with email [%s] already exist.".formatted(request.getEmail()));
        }

        // 2. Празни полета
        if (!StringUtils.hasText(request.getFirstName()) ||
                !StringUtils.hasText(request.getLastName()) ||
                !StringUtils.hasText(request.getEmail()) ||
                !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("All fields are required.");
        }

        // 3. Минимална дължина на паролата
        if (request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        // 4. Проста валидация на имейл
        if (!request.getEmail().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }

    @Override
    public void validateUserLogin(AuthenticationRequest request) {
        if (!StringUtils.hasText(request.getEmail()) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Email and password must not be empty.");
        }

        if (!request.getEmail().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }
}
