package com.spicep.cryptowallet.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class WalletInfoDto {
    private String id;
    private BigDecimal total;
    private List<AssetInfoDto> assets;
}