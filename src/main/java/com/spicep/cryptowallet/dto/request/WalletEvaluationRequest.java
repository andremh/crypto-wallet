package com.spicep.cryptowallet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class WalletEvaluationRequest {
    private List<AssetEvaluationRequestDto> assets;
}