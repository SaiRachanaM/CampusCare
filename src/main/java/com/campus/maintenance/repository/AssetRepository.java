package com.campus.maintenance.repository;

import com.campus.maintenance.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByLocation_LocationId(Long locationId);
    boolean existsByLocation_LocationIdAndAssetId(Long locationId, Long assetId);
}
