package com.spicep.cryptowallet.controller;

import com.spicep.cryptowallet.dto.request.AddAssetRequest;
import com.spicep.cryptowallet.dto.request.CreateWalletRequest;
import com.spicep.cryptowallet.dto.request.WalletEvaluationRequest;
import com.spicep.cryptowallet.dto.request.WalletInfoDto;
import com.spicep.cryptowallet.dto.response.AssetResponseDto;
import com.spicep.cryptowallet.dto.response.CreateWalletResponse;
import com.spicep.cryptowallet.dto.response.WalletEvaluationResponse;
import com.spicep.cryptowallet.entity.Asset;
import com.spicep.cryptowallet.entity.Wallet;
import com.spicep.cryptowallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller for managing wallet-related operations.
 */

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;


    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }


    @PostMapping
    public ResponseEntity<CreateWalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest createWalletRequest) {
        Wallet wallet = walletService.createNewWallet(createWalletRequest.getEmail());
        CreateWalletResponse walletResponseDto = new CreateWalletResponse(wallet.getId(), createWalletRequest.getEmail());
        return new ResponseEntity<>(walletResponseDto, HttpStatus.CREATED);
    }

    @PostMapping("/{walletId}/assets")
    public ResponseEntity<AssetResponseDto> addAsset(@PathVariable Long walletId, @Valid @RequestBody AddAssetRequest addAssetRequest ) {
        Asset asset = walletService.addAsset(walletId, addAssetRequest.getSymbol(), addAssetRequest.getQuantity());
        AssetResponseDto assetResponseDto = new AssetResponseDto(asset.getId(),
                asset.getSymbol(),
                asset.getQuantity(),
                asset.getPrice(),
                asset.getValue());

        return new ResponseEntity<>(assetResponseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletInfoDto> getWalletInfo(@PathVariable Long walletId) {
        WalletInfoDto walletInfoDto = walletService.getWalletInformation(walletId);
        return ResponseEntity.ok(walletInfoDto);
    }


    @PostMapping("/evaluate")
    public ResponseEntity<WalletEvaluationResponse> evaluateWallet(
            @Valid @RequestBody WalletEvaluationRequest request,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        WalletEvaluationResponse evaluation = walletService.evaluateWallet(
                request.getAssets(),
                date != null ? date : LocalDate.now());

        return ResponseEntity.ok(evaluation);
    }
}
