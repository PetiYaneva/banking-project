package com.example.banking_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class BankingProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingProjectApplication.class, args);
	}

}
