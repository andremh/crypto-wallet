package com.spicep.cryptowallet.service;

import com.spicep.cryptowallet.dto.coincap.CoinCapAssetDto;
import com.spicep.cryptowallet.dto.request.WalletInfoDto;
import com.spicep.cryptowallet.entity.*;
import com.spicep.cryptowallet.exception.wallet.WalletAlreadyExistsException;
import com.spicep.cryptowallet.exception.wallet.WalletException;
import com.spicep.cryptowallet.exception.wallet.WalletNotFoundException;
import com.spicep.cryptowallet.repository.AssetRepository;
import com.spicep.cryptowallet.repository.UserRepository;
import com.spicep.cryptowallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Class that tests the wallet service
 */
@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private ApiClientService apiClientService;

    @InjectMocks
    private WalletService walletService;

    /**
     * Wallet creation
     */
    @Test
    void shouldCreateWalletForNewUser() {
        String email = "new@test.com";
        User newUser = new User();
        newUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Wallet wallet = walletService.createNewWallet(email);

        assertNotNull(wallet);
        assertEquals(email, wallet.getUser().getEmail());
        verify(userRepository).save(any(User.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void shouldCreateWalletForExistingUserWithoutWallet() {
        String email = "existing@test.com";
        User existingUser = new User();
        existingUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Wallet wallet = walletService.createNewWallet(email);

        assertNotNull(wallet);
        assertEquals(existingUser, wallet.getUser());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyHasWallet() {
        String email = "alreadyHasWallet@test.com";
        User existingUser = new User();
        existingUser.setEmail(email);
        existingUser.setWallet(new Wallet());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        assertThrows(WalletAlreadyExistsException.class, () -> walletService.createNewWallet(email));
    }

    /**
     * Asset management
     */
    @Test
    void shouldAddNewAssetToWallet() {
        Long walletId = 1L;
        String symbol = "BTC";
        BigDecimal quantity = new BigDecimal("0.5");
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setAssets(new ArrayList<>());

        CoinCapAssetDto assetDto = new CoinCapAssetDto();
        assetDto.setPriceUsd(new BigDecimal("50000"));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(apiClientService.getCurrentAssetDataBySymbol(symbol)).thenReturn(assetDto);
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Asset asset = walletService.addAsset(walletId, symbol, quantity);

        assertNotNull(asset);
        assertEquals(symbol, asset.getSymbol());
        assertEquals(quantity, asset.getQuantity());
        assertEquals(new BigDecimal("50000.00"), asset.getPrice());
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    void shouldUpdateExistingAssetInWallet() {
        Long walletId = 1L;
        String symbol = "BTC";
        BigDecimal initialQuantity = new BigDecimal("0.5");
        BigDecimal additionalQuantity = new BigDecimal("0.3");
        Wallet wallet = new Wallet();
        wallet.setId(walletId);

        Asset existingAsset = new Asset();
        existingAsset.setSymbol(symbol);
        existingAsset.setQuantity(initialQuantity);
        existingAsset.setPrice(new BigDecimal("50000"));
        wallet.setAssets(new ArrayList<>(Collections.singletonList(existingAsset)));

        CoinCapAssetDto assetDto = new CoinCapAssetDto();
        assetDto.setPriceUsd(new BigDecimal("52000"));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(apiClientService.getCurrentAssetDataBySymbol(symbol)).thenReturn(assetDto);
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Asset updatedAsset = walletService.addAsset(walletId, symbol, additionalQuantity);

        assertNotNull(updatedAsset);
        assertEquals(symbol, updatedAsset.getSymbol());
        assertEquals(new BigDecimal("0.8"), updatedAsset.getQuantity());
        assertEquals(new BigDecimal("52000.00"), updatedAsset.getPrice());
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound() {
        Long walletId = 99L;
        String symbol = "BTC";
        BigDecimal quantity = new BigDecimal("0.5");

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.addAsset(walletId, symbol, quantity));
    }

    @Test
    void shouldThrowExceptionWhenAssetNotFound() {
        Long walletId = 1L;
        String symbol = "INVALID";
        BigDecimal quantity = new BigDecimal("0.5");
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setAssets(new ArrayList<>());

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(apiClientService.getCurrentAssetDataBySymbol(symbol)).thenReturn(null);

        assertThrows(WalletException.class, () -> walletService.addAsset(walletId, symbol, quantity));
    }

    /**
     * Wallet evaluation
     */
    @Test
    void shouldReturnWalletInformation() {
        Long walletId = 1L;
        Wallet wallet = new Wallet();
        wallet.setId(walletId);

        Asset btc = new Asset();
        btc.setSymbol("BTC");
        btc.setQuantity(new BigDecimal("0.5"));
        btc.setPrice(new BigDecimal("50000"));
        btc.setWallet(wallet);

        Asset eth = new Asset();
        eth.setSymbol("ETH");
        eth.setQuantity(new BigDecimal("2.0"));
        eth.setPrice(new BigDecimal("2500"));
        eth.setWallet(wallet);
        wallet.setAssets(List.of(btc, eth));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        WalletInfoDto response = walletService.getWalletInformation(walletId);

        assertNotNull(response);
        assertEquals(String.valueOf(walletId), response.getId());
        assertEquals(2, response.getAssets().size());
        assertEquals(new BigDecimal("30000.00"), response.getTotal());
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFoundForGetWalletInformation() {
        Long walletId = 99L;

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.getWalletInformation(walletId));
    }

}