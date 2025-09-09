package net.java.hms_backend.service;

import net.java.hms_backend.dto.AssetDto;
import net.java.hms_backend.dto.AssetFilterRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AssetService {

    AssetDto createAsset(AssetDto dto);

    Page<AssetDto> getAllAssets(int page, int size);

    List<AssetDto> getAssetsByRoom(Long roomId);

    AssetDto updateAsset(Long id, AssetDto dto);

    void deleteAsset(Long id);

    Page<AssetDto> searchAssets(AssetFilterRequest filter, int page, int size);

    AssetDto getAssetById(Long id);
}
