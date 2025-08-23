package com.example.banking_project.web.dto;

import com.example.banking_project.user.model.Employment;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteProfileRequest {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String address;
    private Employment employment;
    private BigDecimal declaredIncome;
}

