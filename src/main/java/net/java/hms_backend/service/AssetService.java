package net.java.hms_backend.service;

import net.java.hms_backend.dto.AssetDto;

import java.util.List;

public interface AssetService {

    AssetDto createAsset(AssetDto dto);

    List<AssetDto> getAllAssets();

    List<AssetDto> getAssetsByRoom(Long roomId);

    AssetDto updateAsset(Long id, AssetDto dto);

    void deleteAsset(Long id);
}
