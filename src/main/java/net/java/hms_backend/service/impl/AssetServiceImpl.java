package net.java.hms_backend.service.impl;

import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.AssetDto;
import net.java.hms_backend.dto.AssetFilterRequestDto;
import net.java.hms_backend.entity.Asset;
import net.java.hms_backend.entity.Room;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.AssetMapper;
import net.java.hms_backend.repository.AssetRepository;
import net.java.hms_backend.repository.RoomRepository;
import net.java.hms_backend.service.AssetService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final RoomRepository roomRepository;

    @Override
    public AssetDto createAsset(AssetDto dto) {
        Integer roomNumber = dto.getRoomNumber();
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "roomNumber", roomNumber));

        Asset asset = AssetMapper.toEntity(dto, room);
        Asset saved = assetRepository.save(asset);
        return AssetMapper.toDto(saved);
    }

    @Override
    public List<AssetDto> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(AssetMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssetDto> getAssetsByRoom(Long roomId) {
        return assetRepository.findByRoomId(roomId).stream()
                .map(AssetMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AssetDto updateAsset(Long id, AssetDto dto) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));

        asset.setName(dto.getName());
        asset.setCategory(dto.getCategory());
        asset.setCondition(dto.getCondition());
        asset.setOriginalCost(dto.getOriginalCost());
        asset.setPurchaseDate(dto.getPurchaseDate());
        asset.setNote(dto.getNote());

        return AssetMapper.toDto(assetRepository.save(asset));
    }

    @Override
    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));
        assetRepository.delete(asset);
    }

    @Override
    public List<AssetDto> searchAssets(AssetFilterRequestDto filter) {
        List<Asset> assets = assetRepository.findAll();
        Stream<Asset> stream = assets.stream();

        if (filter.getName() != null) {
            stream = stream.filter(a -> a.getName().toLowerCase().contains(filter.getName().toLowerCase()));
        }

        if (filter.getCategory() != null) {
            stream = stream.filter(a -> a.getCategory().equalsIgnoreCase(filter.getCategory()));
        }

        if (filter.getCondition() != null) {
            stream = stream.filter(a -> a.getCondition().equalsIgnoreCase(filter.getCondition()));
        }

        if (filter.getMinCost() != null) {
            stream = stream.filter(a -> a.getOriginalCost() >= filter.getMinCost());
        }

        if (filter.getMaxCost() != null) {
            stream = stream.filter(a -> a.getOriginalCost() <= filter.getMaxCost());
        }

        if (filter.getPurchaseDateFrom() != null) {
            stream = stream.filter(a -> !a.getPurchaseDate().isBefore(filter.getPurchaseDateFrom()));
        }

        if (filter.getPurchaseDateTo() != null) {
            stream = stream.filter(a -> !a.getPurchaseDate().isAfter(filter.getPurchaseDateTo()));
        }

        if (filter.getRoomNumber() != null) {
            stream = stream.filter(a -> a.getRoom().getRoomNumber().equals(filter.getRoomNumber()));
        }

        return stream.map(AssetMapper::toDto).collect(Collectors.toList());
    }

}
