package com.spicep.cryptowallet.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetInfoDto {
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal value;
}