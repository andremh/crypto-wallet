package com.spicep.cryptowallet.service;

import com.spicep.cryptowallet.dto.coincap.CoinCapAssetDto;
import com.spicep.cryptowallet.dto.coincap.CoinCapAssetHistoryDto;
import com.spicep.cryptowallet.dto.request.AssetEvaluationRequestDto;
import com.spicep.cryptowallet.dto.request.AssetInfoDto;
import com.spicep.cryptowallet.dto.request.WalletInfoDto;
import com.spicep.cryptowallet.dto.response.WalletEvaluationResponse;
import com.spicep.cryptowallet.entity.Asset;
import com.spicep.cryptowallet.entity.User;
import com.spicep.cryptowallet.entity.Wallet;
import com.spicep.cryptowallet.exception.wallet.WalletAlreadyExistsException;
import com.spicep.cryptowallet.exception.wallet.WalletException;
import com.spicep.cryptowallet.exception.wallet.WalletNotFoundException;
import com.spicep.cryptowallet.repository.AssetRepository;
import com.spicep.cryptowallet.repository.UserRepository;
import com.spicep.cryptowallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final ApiClientService clientService;

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);
    private static final String DEFAULT_ASSET = "N/A";
    private static final int PERCENTAGE_SCALE = 2;

    public WalletService(WalletRepository walletRepository, UserRepository userRepository, ApiClientService clientService, AssetRepository assetRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.clientService = clientService;
        this.assetRepository = assetRepository;
    }


    @Transactional
    public Wallet createNewWallet(String email) {
        User existingUser = userRepository.findByEmail(email).orElse(null);

        if(existingUser != null) {
            if(existingUser.getWallet() != null) {
                throw new WalletAlreadyExistsException("User already has a wallet");
            }

            Wallet wallet = new Wallet();
            wallet.setUser(existingUser);
            existingUser.setWallet(wallet);

            return walletRepository.save(wallet);
        } else {
            User newUser = new User();
            newUser.setEmail(email);
            userRepository.save(newUser);

            Wallet wallet = new Wallet();
            wallet.setUser(newUser);

            newUser.setWallet(wallet);
            return walletRepository.save(wallet);
        }
    }

    @Transactional
    public Asset addAsset(Long walletId, String symbol, BigDecimal quantity) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + walletId));

        CoinCapAssetDto assetDto = clientService.getCurrentAssetDataBySymbol(symbol);
        if(assetDto == null || assetDto.getPriceUsd() == null) {
            throw new WalletException("Cannot add asset. Symbol not found: " + symbol);
        }
        BigDecimal currentPrice = assetDto.getPriceUsd();

        Asset existingAsset = wallet.getAssets().stream()
                .filter(a -> a.getSymbol().equals(symbol))
                .findFirst().orElse(null);

        if (existingAsset != null) {
            existingAsset.addQuantity(quantity);
            existingAsset.setPrice(currentPrice.setScale(2, RoundingMode.HALF_UP));
            return assetRepository.save(existingAsset);
        } else {
            Asset newAsset = new Asset();
            newAsset.setSymbol(symbol);
            newAsset.setQuantity(quantity);
            newAsset.setPrice(currentPrice.setScale(2, RoundingMode.HALF_UP));
            newAsset.setWallet(wallet);

            wallet.getAssets().add(newAsset);
            return assetRepository.save(newAsset);
        }
    }

    @Transactional
    public WalletInfoDto getWalletInformation(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + walletId));

        List<AssetInfoDto> assetInfoList = new ArrayList<>();
        BigDecimal totalValue = BigDecimal.ZERO;

        //Total value calculation
        for (Asset asset : wallet.getAssets()) {
            BigDecimal value = asset.getValue();
            totalValue = totalValue.add(value);

            AssetInfoDto assetInfo = new AssetInfoDto();
            assetInfo.setSymbol(asset.getSymbol());
            assetInfo.setQuantity(asset.getQuantity());
            assetInfo.setPrice(asset.getPrice());
            assetInfo.setValue(value);

            assetInfoList.add(assetInfo);
        }

        WalletInfoDto walletInfo = new WalletInfoDto();
        walletInfo.setId(wallet.getId().toString());
        walletInfo.setAssets(assetInfoList);
        walletInfo.setTotal(totalValue);

        return walletInfo;
    }

    /**
     * Evaluates a wallet's performance by comparing provided asset values with their values on a specific date.
     *
     * @param inputAssets List of assets and their values
     * @param referenceDate Date to evaluate assets against (defaults to today if null)
     * @return Evaluation result with total value and performance metrics
     */
    public WalletEvaluationResponse evaluateWallet(List<AssetEvaluationRequestDto> inputAssets, LocalDate referenceDate) {
        if (inputAssets == null || inputAssets.isEmpty()) {
            log.warn("Wallet evaluation attempted with empty asset list");
            throw new WalletException("Asset list cannot be empty");
        }

        log.info("Evaluating wallet with {} assets for date {}", inputAssets.size(), referenceDate);

        // Get asset values based on reference date prices
        List<AssetEvaluationRequestDto> assetsWithReferenceDateValues = inputAssets.stream().map(inputAsset -> {
            String symbol = inputAsset.getSymbol();
            BigDecimal quantity = inputAsset.getQuantity();

            log.debug("Processing asset: {} with quantity: {}", symbol, quantity);

            List<CoinCapAssetHistoryDto> historicalPrices;
            if (referenceDate == null || referenceDate.isEqual(LocalDate.now())) {

                // For current date, use the current price API
                CoinCapAssetDto currentAssetData = clientService.getCurrentAssetDataBySymbol(symbol);

                CoinCapAssetHistoryDto currentPriceData = new CoinCapAssetHistoryDto();
                currentPriceData.setPriceUsd(currentAssetData.getPriceUsd());
                currentPriceData.setTime(System.currentTimeMillis());

                historicalPrices = Collections.singletonList(currentPriceData);
            } else {

                // For historical date, use the date-based API
                historicalPrices = clientService.getAssetPriceForDate(
                        symbol,
                        ApiClientService.CoinCapInterval.DAY_1.getValue(),
                        referenceDate);
            }

            if (historicalPrices.isEmpty()) {
                log.error("No price data available for {} on {}", symbol, referenceDate);
                throw new WalletException("No price data available for " + symbol + " on " + referenceDate);
            }

            //Get the most recent price for that day
            CoinCapAssetHistoryDto priceData = historicalPrices.get(0);
            BigDecimal priceOnDate = priceData.getPriceUsd();
            BigDecimal valueOnReferenceDate = quantity.multiply(priceOnDate);

            log.debug("Retrieved price for {}: {} on date {}, calculated value: {}",
                    symbol, priceOnDate, referenceDate, valueOnReferenceDate);

            return new AssetEvaluationRequestDto(symbol, quantity, valueOnReferenceDate);
        }).toList();

        // Calculate performance metrics
        Map<String, BigDecimal> performanceByAsset = new HashMap<>();
        log.debug("Calculating performance metrics");

        for (int i = 0; i < inputAssets.size(); i++) {
            String symbol = inputAssets.get(i).getSymbol();
            BigDecimal providedValue = inputAssets.get(i).getValue();
            BigDecimal referenceValue = assetsWithReferenceDateValues.get(i).getValue();

            BigDecimal performance = calculatePerformance(referenceValue, providedValue);
            performanceByAsset.put(symbol, performance);

        }

        // Find best/worst performing assets and totals
        Optional<Map.Entry<String, BigDecimal>> bestPerformance =
                performanceByAsset.entrySet().stream().max(Map.Entry.comparingByValue());
        Optional<Map.Entry<String, BigDecimal>> worstPerformance =
                performanceByAsset.entrySet().stream().min(Map.Entry.comparingByValue());

        BigDecimal totalValue = assetsWithReferenceDateValues.stream()
                .map(AssetEvaluationRequestDto::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

        log.info("Wallet evaluation complete. Total value: {}", totalValue);

        String bestAsset = bestPerformance.map(Map.Entry::getKey).orElse(DEFAULT_ASSET);
        BigDecimal bestValue = bestPerformance
                .map(e -> e.getValue().setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);

        String worstAsset = worstPerformance.map(Map.Entry::getKey).orElse(DEFAULT_ASSET);
        BigDecimal worstValue = worstPerformance
                .map(e -> e.getValue().setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);

        return new WalletEvaluationResponse(totalValue, bestAsset, bestValue, worstAsset, worstValue);
    }

    /**
     * Calculates the percentage performance between two values.
     * Performance = ((reference value - provided value) / provided value) * 100
     *
     * @param currentValue The new value
     * @param pastValue The reference value
     * @return The performance percentage
     */
    private BigDecimal calculatePerformance(BigDecimal currentValue, BigDecimal pastValue) {
        if (pastValue == null || pastValue.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Attempted to calculate performance with zero or null past value");
            return BigDecimal.ZERO;
        }

        return currentValue.subtract(pastValue)
                .multiply(BigDecimal.valueOf(100))
                .divide(pastValue, 4, RoundingMode.HALF_UP);
    }
}
