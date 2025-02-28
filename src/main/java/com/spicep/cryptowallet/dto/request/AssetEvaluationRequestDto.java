package com.spicep.cryptowallet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class AssetEvaluationRequestDto {
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal value;
}