package com.spicep.cryptowallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class AssetResponseDto {
    private Long id;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal value;
    
}