package com.spicep.cryptowallet.repository;

import com.spicep.cryptowallet.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UserRepositoryTest extends RepositoryTestBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateAndFindUser() {
        User user = createTestUser();

        User savedUser = userRepository.save(user);
        
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        
        assertTrue(foundUser.isPresent());
        assertEquals(user.getEmail(), foundUser.get().getEmail());
    }
    
    @Test
    void testFindByEmail() {
        User user = createTestUser();
        userRepository.save(user);

        String email = user.getEmail();
        Optional<User> foundUser = userRepository.findByEmail(email);
        
        assertTrue(foundUser.isPresent());
        assertEquals(email, foundUser.get().getEmail());
    }
}