package com.spicep.cryptowallet.service;

import com.spicep.cryptowallet.dto.coincap.CoinCapAssetDto;
import com.spicep.cryptowallet.dto.coincap.CoinCapAssetHistoryDto;
import com.spicep.cryptowallet.dto.coincap.CoinCapAssetHistoryResponseDto;
import com.spicep.cryptowallet.dto.coincap.CoinCapAssetResponseDto;
import com.spicep.cryptowallet.exception.asset.AssetNotFoundException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

/**
 * Service class to interact with the CoinCap API.
 * <a href="https://docs.coincap.io/"/>
 */
@Service
public class ApiClientService {

    private static final Logger log = LoggerFactory.getLogger(ApiClientService.class);
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final String BASE_URL = "https://api.coincap.io/v2";

    private final WebClient webClient;

    /**
     * Enum for CoinCap API intervals
     */
    @Getter
    public enum CoinCapInterval {
        MINUTE_1("m1"),
        MINUTE_5("m5"),
        MINUTE_15("m15"),
        MINUTE_30("m30"),
        HOUR_1("h1"),
        HOUR_2("h2"),
        HOUR_6("h6"),
        HOUR_12("h12"),
        DAY_1("d1");

        private final String value;

        CoinCapInterval(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    // Constructor needed for tests
    private ApiClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Autowired
    public ApiClientService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Retrieves asset information by its symbol.
     *
     * @param symbol The asset symbol
     * @return CoinCapAssetDto containing asset information
     * @throws AssetNotFoundException if the asset is not found
     */
    public CoinCapAssetDto getCurrentAssetDataBySymbol(String symbol) {
        CoinCapAssetResponseDto response = webClient.get()
                .uri("/assets/{id}", symbol.toLowerCase())
                .retrieve()
                .bodyToMono(CoinCapAssetResponseDto.class)
                .block();

        if (response != null && response.getData() != null) {
            return response.getData();
        }
        throw new AssetNotFoundException("Asset not found for symbol: " + symbol);
    }


    public BigDecimal getAssetPrice(String symbol) {
        CoinCapAssetDto asset = getCurrentAssetDataBySymbol(symbol);
        return asset.getPriceUsd();
    }


    /**
     * Retrieves historical price data for a specific asset on a given date.
     * Taken from Coincap api
     * Example <a href="https://api.coincap.io/v2/assets/bitcoin/history?interval=d1&start=1672617600000&end=1672704000000"/>
     *
     * @param symbol The asset symbol
     * @param interval The time interval
     * @param date The date to get prices for
     * @return List of price data for the specified date
     */
    public List<CoinCapAssetHistoryDto> getAssetPriceForDate(String symbol, String interval, LocalDate date) {

        long startMillis = date.atStartOfDay(UTC_ZONE).toInstant().toEpochMilli();
        long endMillis = date.atStartOfDay(UTC_ZONE).plusDays(1).toInstant().toEpochMilli();

        try {
            CoinCapAssetHistoryResponseDto response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/assets/{id}/history")
                            .queryParam("interval", interval)
                            .queryParam("start", startMillis)
                            .queryParam("end", endMillis)
                            .build(symbol.toLowerCase()))
                    .retrieve()
                    .bodyToMono(CoinCapAssetHistoryResponseDto.class)
                    .block();

            if (response != null && response.getData() != null) {
                return response.getData();
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching price data for {} on {}: {}", symbol, date, e.getMessage());
            return Collections.emptyList();
        }
    }
}
