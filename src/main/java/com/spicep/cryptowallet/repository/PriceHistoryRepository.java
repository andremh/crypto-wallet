package com.spicep.cryptowallet.repository;

import com.spicep.cryptowallet.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long > {

    List<PriceHistory> findAllBySymbol(String symbol);
}
