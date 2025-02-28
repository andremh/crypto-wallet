package com.spicep.cryptowallet.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity class representing the price history of an asset.
 * This class is independent of the assets to keep records of deleted ones, for example.
 */
@Data
@Entity
@Table(name="price_history")
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "date")
    private Instant timestamp;
}
