package com.spicep.cryptowallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spicep.cryptowallet.dto.request.*;
import com.spicep.cryptowallet.dto.response.AssetResponseDto;
import com.spicep.cryptowallet.dto.response.CreateWalletResponse;
import com.spicep.cryptowallet.dto.response.WalletEvaluationResponse;
import com.spicep.cryptowallet.entity.Asset;
import com.spicep.cryptowallet.entity.Wallet;
import com.spicep.cryptowallet.exception.wallet.WalletException;
import com.spicep.cryptowallet.exception.wallet.WalletNotFoundException;
import com.spicep.cryptowallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateWallet() throws Exception {
        CreateWalletRequest request = new CreateWalletRequest("test@example.com");
        CreateWalletResponse response = new CreateWalletResponse(1L, "test@example.com");
        Wallet mockedWallet = new Wallet();
        mockedWallet.setId(1L);

        when(walletService.createNewWallet(anyString())).thenReturn(mockedWallet);

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));
    }

    @Test
    void shouldAddAssetToWallet() throws Exception {
        long walletId = 1L;
        AddAssetRequest request = new AddAssetRequest("BTC", new BigDecimal("1.0"));
        Asset mockedAsset = new Asset();
        mockedAsset.setId(1L);
        mockedAsset.setSymbol("BTC");
        mockedAsset.setQuantity(BigDecimal.ONE);
        mockedAsset.setPrice(new BigDecimal("30000.00"));
        AssetResponseDto responseDto = new AssetResponseDto(mockedAsset.getId(), mockedAsset.getSymbol(), mockedAsset.getQuantity(), mockedAsset.getPrice(), mockedAsset.getValue());


        when(walletService.addAsset(anyLong(), anyString(), any(BigDecimal.class))).thenReturn(mockedAsset);

        mockMvc.perform(post("/api/wallets/{walletId}/assets", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.symbol").value(responseDto.getSymbol()))
                .andExpect(jsonPath("$.quantity").value(responseDto.getQuantity().doubleValue()))
                .andExpect(jsonPath("$.price").value(responseDto.getPrice().doubleValue()))
                .andExpect(jsonPath("$.value").value(responseDto.getValue().doubleValue()));
    }

    @Test
    void shouldReturnNotFoundAddingAssetWhenWalletDoesNotExist() throws Exception {
        long nonExistentWalletId = 999L;
        AddAssetRequest request = new AddAssetRequest("BTC", new BigDecimal("1.0"));

        when(walletService.addAsset(eq(nonExistentWalletId), anyString(), any(BigDecimal.class)))
                .thenThrow(new WalletNotFoundException("Wallet not found with id: " + nonExistentWalletId));

        mockMvc.perform(post("/api/wallets/{walletId}/assets", nonExistentWalletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenGetWalletInfoForNonExistentWallet() throws Exception {
        long nonExistentWalletId = 999L;

        when(walletService.getWalletInformation(nonExistentWalletId))
                .thenThrow(new WalletNotFoundException("Wallet not found with id: " + nonExistentWalletId));

        mockMvc.perform(get("/api/wallets/{walletId}", nonExistentWalletId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnWalletInfoSuccessfully() throws Exception {
        long walletId = 1L;
        WalletInfoDto mockedWalletInfoDto = new WalletInfoDto();
        mockedWalletInfoDto.setId(String.valueOf(walletId));
        mockedWalletInfoDto.setTotal(new BigDecimal("1000.00"));
        AssetInfoDto assetInfoDto1 = new AssetInfoDto();
        assetInfoDto1.setSymbol("BTC");
        assetInfoDto1.setQuantity(BigDecimal.valueOf(0.05));
        assetInfoDto1.setPrice(new BigDecimal("20000.00"));
        assetInfoDto1.setValue(new BigDecimal("1000.00"));
        mockedWalletInfoDto.setAssets(List.of(assetInfoDto1));


        when(walletService.getWalletInformation(walletId)).thenReturn(mockedWalletInfoDto);

        mockMvc.perform(get("/api/wallets/{walletId}", walletId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(walletId)))
                .andExpect(jsonPath("$.total").value(1000.00))
                .andExpect(jsonPath("$.assets[0].symbol").value("BTC"))
                .andExpect(jsonPath("$.assets[0].quantity").value(0.05))
                .andExpect(jsonPath("$.assets[0].price").value(20000.00))
                .andExpect(jsonPath("$.assets[0].value").value(1000.00));
    }

    @Test
    void shouldEvaluateWalletSuccessfully() throws Exception {
        WalletEvaluationRequest request = new WalletEvaluationRequest(List.of(
                new com.spicep.cryptowallet.dto.request.AssetEvaluationRequestDto("BTC", BigDecimal.ONE, BigDecimal.valueOf(30000))
        ));

        WalletEvaluationResponse mockResponse = new WalletEvaluationResponse(
                BigDecimal.valueOf(30000), "BTC", BigDecimal.ZERO, "ADA", BigDecimal.ZERO);

        when(walletService.evaluateWallet(any(), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/wallets/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(30000))
                .andExpect(jsonPath("$.bestAsset").value("BTC"))
                .andExpect(jsonPath("$.worstAsset").value("ADA"));
    }

    @Test
    void shouldReturnBadRequestForEvaluateWalletWithEmptyAssets() throws Exception {
        WalletEvaluationRequest request = new WalletEvaluationRequest(List.of());

        when(walletService.evaluateWallet(any(), any()))
                .thenThrow(new WalletException("Asset list cannot be empty"));

        mockMvc.perform(post("/api/wallets/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
