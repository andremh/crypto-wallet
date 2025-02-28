package com.spicep.cryptowallet.scheduler;


import com.spicep.cryptowallet.entity.Asset;
import com.spicep.cryptowallet.entity.PriceHistory;
import com.spicep.cryptowallet.repository.AssetRepository;
import com.spicep.cryptowallet.repository.PriceHistoryRepository;
import com.spicep.cryptowallet.service.ApiClientService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Class responsible for periodically updating the prices of assets.
 * It fetches the latest prices from an external API and updates the asset prices in the database.
 */
@Component
public class PriceUpdateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PriceUpdateScheduler.class);

    private final ApiClientService clientService;
    private final AssetRepository assetRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private ThreadPoolTaskExecutor taskExecutor;

    @Value("${crypto.update.max-threads:3}")
    private int maxThreads;

    public PriceUpdateScheduler(ApiClientService clientService,
                                AssetRepository assetRepository,
                                PriceHistoryRepository priceHistoryRepository) {
        this.clientService = clientService;
        this.assetRepository = assetRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @PostConstruct
    public void initialize() {

        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(maxThreads);
        this.taskExecutor.setMaxPoolSize(maxThreads);
        this.taskExecutor.setThreadNamePrefix("price-updater-");
        this.taskExecutor.initialize();

        logger.info("Price update scheduler initialized with {} threads", maxThreads);
    }

    /**
     * Periodically updates the prices of all assets.
     */
    @Scheduled(fixedRateString = "${crypto.update.rate:60000}")
    public void updatePrices() {
        logger.info("Starting scheduled price update");

        List<String> uniqueSymbols = assetRepository.findAll().stream()
                .map(Asset::getSymbol)
                .distinct()
                .toList();

        if (uniqueSymbols.isEmpty()) {
            logger.info("No tokens to update");
            return;
        }

        logger.info("Found {} unique tokens to update", uniqueSymbols.size());

        for (int i = 0; i < uniqueSymbols.size(); i += maxThreads) {
            int endIndex = Math.min(i + maxThreads, uniqueSymbols.size());
            List<String> batch = uniqueSymbols.subList(i, endIndex);

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String symbol : batch) {
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        updateSingleToken(symbol);
                    } catch (Exception e) {
                        logger.error("Error updating price for {}", symbol, e);
                    }
                }, taskExecutor));
            }

            // Wait for all tokens in this batch to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        logger.info("Price update completed");
    }

    /**
     * Updates the price of a single token.
     * @param symbol The token symbol
     */
    private void updateSingleToken(String symbol) {
        logger.info("Updating price for: {}", symbol);

        try {
            BigDecimal currentPrice = clientService.getAssetPrice(symbol);

            PriceHistory priceHistory = new PriceHistory();
            priceHistory.setSymbol(symbol);
            priceHistory.setPrice(currentPrice);
            priceHistory.setTimestamp(Instant.now());
            priceHistoryRepository.save(priceHistory);

            List<Asset> assets = assetRepository.findBySymbol(symbol);
            for (Asset asset : assets) {
                asset.setPrice(currentPrice);
                assetRepository.save(asset);
            }

            logger.info("Updated price for {} to {}", symbol, currentPrice);
        } catch (Exception e) {
            logger.error("Failed to update price for {}", symbol, e);
        }
    }
}