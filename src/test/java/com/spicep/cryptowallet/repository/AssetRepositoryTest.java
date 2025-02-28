package com.spicep.cryptowallet.repository;

import com.spicep.cryptowallet.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AssetRepositoryTest extends RepositoryTestBase {

    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testSaveAndFindAsset() {
        User user = createTestUser();
        userRepository.save(user);
        
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        user.setWallet(wallet);
        walletRepository.save(wallet);
        
        Asset asset = new Asset();
        asset.setSymbol("BTC");
        asset.setQuantity(new BigDecimal("0.5"));
        asset.setPrice(new BigDecimal("50000.00"));
        asset.setWallet(wallet);
        
        Asset savedAsset = assetRepository.save(asset);
        
        Optional<Asset> foundAsset = assetRepository.findById(savedAsset.getId());
        
        assertTrue(foundAsset.isPresent());
        assertEquals("BTC", foundAsset.get().getSymbol());
        assertEquals(0, new BigDecimal("0.5").compareTo(foundAsset.get().getQuantity()));
        assertEquals(0, new BigDecimal("50000.00").compareTo(foundAsset.get().getPrice()));
    }
    
    @Test
    void testFindBySymbol() {
        User user = createTestUser();
        userRepository.save(user);
        
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        user.setWallet(wallet);
        walletRepository.save(wallet);
        
        Asset asset = new Asset();
        asset.setSymbol("ETH");
        asset.setQuantity(new BigDecimal("2.0"));
        asset.setPrice(new BigDecimal("3000.00"));
        asset.setWallet(wallet);
        assetRepository.save(asset);
        
        List<Asset> foundAssets = assetRepository.findBySymbol("ETH");
        
        assertFalse(foundAssets.isEmpty());
        assertEquals("ETH", foundAssets.get(0).getSymbol());
    }
}