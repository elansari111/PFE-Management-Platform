package com.pfe.management;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PfeManagementApplicationTests {

    @Test
    @DisplayName("Le contexte Spring Boot doit démarrer sans erreur")
    void contextLoads() {
    }
}
