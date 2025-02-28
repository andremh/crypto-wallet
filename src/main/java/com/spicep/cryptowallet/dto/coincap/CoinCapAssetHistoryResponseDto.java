package com.spicep.cryptowallet.dto.coincap;

import lombok.Data;

import java.util.List;

@Data
public class CoinCapAssetHistoryResponseDto {
    private List<CoinCapAssetHistoryDto> data;
    private long timestamp;
}