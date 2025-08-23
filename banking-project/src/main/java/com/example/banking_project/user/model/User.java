package com.example.banking_project.user.model;

import com.example.banking_project.account.model.Account;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role")
    private UserRole role;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "created_on", nullable = false)
    private LocalDate createdOn;

    @Column(name = "updated_on", nullable = false)
    private LocalDate updatedOn;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment")
    private Employment employment;

    @Column(name = "declared_income")
    private BigDecimal declaredIncome;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Account> accounts;

    // Ново: флаг за двуфазен онбординг
    @Column(name = "profile_completed", nullable = false)
    private boolean profileCompleted;

    @PrePersist
    public void prePersist() {
        LocalDate now = LocalDate.now();
        this.createdOn = now;
        this.updatedOn = now;
        if (role == null) role = UserRole.USER;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDate.now();
    }
}
