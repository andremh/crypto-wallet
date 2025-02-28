package com.spicep.cryptowallet.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Class that represents an asset in the cryptocurrency wallet.
 */
@Data
@Entity
@Table(name="asset")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    public BigDecimal getValue() {
        if (price == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }

    public void addQuantity(BigDecimal additionalQuantity) {
        if (additionalQuantity == null || additionalQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Additional quantity must be positive");
        }

        if (this.quantity == null) {
            this.quantity = additionalQuantity;
        } else {
            this.quantity = this.quantity.add(additionalQuantity);
        }
    }

}
