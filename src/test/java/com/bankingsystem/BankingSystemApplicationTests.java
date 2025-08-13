package com.bankingsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test to verify the application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class BankingSystemApplicationTests {

    @Test
    void contextLoads() {
        // This test will pass if the Spring Boot context loads successfully
        // It verifies that all beans are properly configured and injectable
    }
}
