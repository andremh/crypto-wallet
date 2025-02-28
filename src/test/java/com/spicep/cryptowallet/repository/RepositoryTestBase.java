package com.spicep.cryptowallet.repository;

import com.spicep.cryptowallet.entity.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class RepositoryTestBase {
    
    protected String generateUniqueEmail() {
        return "test-" + System.currentTimeMillis() + "@spicep.com";
    }
    
    protected User createTestUser() {
        User user = new User();
        user.setEmail(generateUniqueEmail());
        return user;
    }
}