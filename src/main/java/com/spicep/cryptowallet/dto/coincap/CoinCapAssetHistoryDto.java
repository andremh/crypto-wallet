package com.spicep.cryptowallet.dto.coincap;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoinCapAssetHistoryDto {
    private BigDecimal priceUsd;
    private long time;
}