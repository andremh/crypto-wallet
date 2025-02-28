package com.spicep.cryptowallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateWalletResponse {
    private Long id;
    private String email;

}