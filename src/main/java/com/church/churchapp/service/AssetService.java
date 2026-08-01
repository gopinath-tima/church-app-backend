package com.church.churchapp.service;

import com.church.churchapp.entity.Asset;
import com.church.churchapp.entity.AssetHistory;
import com.church.churchapp.repository.AssetRepository;
import com.church.churchapp.repository.AssetHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetHistoryRepository assetHistoryRepository;

    @Transactional(readOnly = true)
    public List<Asset> getAllAssets() {
        List<Asset> assets = assetRepository.findAll();
        // Optionally recalculate depreciation on read
        assets.forEach(this::calculateDepreciation);
        return assets;
    }

    @Transactional(readOnly = true)
    public Optional<Asset> getAssetById(Long id) {
        return assetRepository.findById(id).map(asset -> {
            calculateDepreciation(asset);
            return asset;
        });
    }

    @Transactional
    public Asset createAsset(Asset asset) {
        calculateDepreciation(asset);
        Asset savedAsset = assetRepository.save(asset);
        logHistory(savedAsset.getAssetId(), "Created", "Registered new asset: " + savedAsset.getName());
        return savedAsset;
    }

    @Transactional
    public Asset updateAsset(Long id, Asset updatedAsset) {
        return assetRepository.findById(id).map(existing -> {
            boolean statusChanged = !existing.getStatus().equals(updatedAsset.getStatus());
            String oldStatus = existing.getStatus();
            
            existing.setName(updatedAsset.getName());
            existing.setCategory(updatedAsset.getCategory());
            existing.setPurchaseDate(updatedAsset.getPurchaseDate());
            existing.setPurchasePrice(updatedAsset.getPurchasePrice());
            existing.setWarrantyExpirationDate(updatedAsset.getWarrantyExpirationDate());
            existing.setNextMaintenanceDate(updatedAsset.getNextMaintenanceDate());
            existing.setMaintenanceFrequencyMonths(updatedAsset.getMaintenanceFrequencyMonths());
            existing.setDepreciationRate(updatedAsset.getDepreciationRate());
            existing.setStatus(updatedAsset.getStatus());
            existing.setNotes(updatedAsset.getNotes());
            
            calculateDepreciation(existing);
            Asset savedAsset = assetRepository.save(existing);
            
            if (statusChanged) {
                logHistory(savedAsset.getAssetId(), "Status Changed", "Status updated from '" + oldStatus + "' to '" + savedAsset.getStatus() + "'");
            } else {
                logHistory(savedAsset.getAssetId(), "Updated", "Asset details updated.");
            }
            
            return savedAsset;
        }).orElseThrow(() -> new RuntimeException("Asset not found"));
    }

    @Transactional
    public void deleteAsset(Long id) {
        assetRepository.deleteById(id);
    }

    private void logHistory(Long assetId, String action, String description) {
        AssetHistory history = new AssetHistory();
        history.setAssetId(assetId);
        history.setAction(action);
        history.setDescription(description);
        history.setActionDate(LocalDateTime.now());
        assetHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<AssetHistory> getAssetHistory(Long assetId) {
        return assetHistoryRepository.findByAssetIdOrderByActionDateDesc(assetId);
    }

    private void calculateDepreciation(Asset asset) {
        if (asset.getPurchasePrice() != null && asset.getDepreciationRate() != null && asset.getPurchaseDate() != null && !asset.getPurchaseDate().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate purchase = LocalDate.parse(asset.getPurchaseDate(), formatter);
                LocalDate now = LocalDate.now();
                
                long yearsElapsed = ChronoUnit.YEARS.between(purchase, now);
                if (yearsElapsed > 0) {
                    double rate = asset.getDepreciationRate() / 100.0;
                    double depreciatedValue = asset.getPurchasePrice() * Math.pow((1 - rate), yearsElapsed);
                    asset.setCurrentValue(Math.max(0, depreciatedValue));
                } else {
                    asset.setCurrentValue(asset.getPurchasePrice());
                }
            } catch (Exception e) {
                // If date parsing fails, fallback to purchase price
                asset.setCurrentValue(asset.getPurchasePrice());
            }
        } else if (asset.getPurchasePrice() != null && asset.getCurrentValue() == null) {
             asset.setCurrentValue(asset.getPurchasePrice());
        }
    }
}
