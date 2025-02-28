package com.spicep.cryptowallet.dto.coincap;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Taken from api.coincap.io/v2/assets/{symbol} response
 */
@Data
public class CoinCapAssetDto {
    private String id;
    private String rank;
    private String symbol;
    private String name;
    private BigDecimal supply;
    private BigDecimal maxSupply;
    private BigDecimal marketCapUsd;
    private BigDecimal volumeUsd24Hr;
    private BigDecimal priceUsd;
    private BigDecimal changePercent24Hr;
    private BigDecimal vwap24Hr;
}