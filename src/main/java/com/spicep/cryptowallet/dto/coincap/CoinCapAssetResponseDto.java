package com.spicep.cryptowallet.dto.coincap;

import lombok.Data;

/**
 * Taken from api.coincap.io/v2/assets/{symbol} response
 */
@Data
public class CoinCapAssetResponseDto {
    private CoinCapAssetDto data;
    private long timestamp;
}