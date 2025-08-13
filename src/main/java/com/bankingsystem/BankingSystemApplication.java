package com.bankingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for the Mlfts Bank API.
 * 
 * This Spring Boot application provides a complete banking backend system
 * with features including:
 * - User authentication and authorization
 * - Account management (checking, savings, business)
 * - Transaction processing
 * - Loan management
 * - Credit/Debit card management
 * - Payment processing
 * - Security and fraud detection
 * - Reporting and analytics
 * 
 * @author Banking System Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class BankingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingSystemApplication.class, args);
        System.out.println("🏦 Mlfts Bank API Started Successfully!");
        System.out.println("📚 API Documentation: http://localhost:8080/api/v1/swagger-ui.html");
        System.out.println("🗄️ H2 Database Console: http://localhost:8080/api/v1/h2-console");
    }
}
