package com.spicep.cryptowallet.repository;

import com.spicep.cryptowallet.entity.User;
import com.spicep.cryptowallet.entity.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class WalletRepositoryTest extends RepositoryTestBase {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateAndFindWallet() {
        User user = createTestUser();
        userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        user.setWallet(wallet);

        Wallet savedWallet = walletRepository.save(wallet);

        Optional<Wallet> foundWallet = walletRepository.findById(savedWallet.getId());

        assertTrue(foundWallet.isPresent());
        assertEquals(user.getId(), foundWallet.get().getUser().getId());
    }

}