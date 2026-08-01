package com.church.churchapp.repository;

import com.church.churchapp.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByCategory(String category);
    List<Asset> findByStatus(String status);
}
