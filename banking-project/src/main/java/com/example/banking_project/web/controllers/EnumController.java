package com.example.banking_project.web.controllers;

import com.example.banking_project.user.model.Employment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enums")
public class EnumController {

    @GetMapping("/employment")
    public Employment[] getEmploymentTypes() {
        return Employment.values();
    }
}

