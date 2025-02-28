package com.spicep.cryptowallet.repository;

import com.spicep.cryptowallet.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset,Long> {

    Optional<Asset> findByWalletIdAndSymbol (Long wallet_id, String symbol);

    List<Asset> findBySymbol(String symbol);
}
