package net.java.hms_backend.service.impl;

import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.AssetDto;
import net.java.hms_backend.dto.AssetFilterRequest;
import net.java.hms_backend.entity.ActivityLog;
import net.java.hms_backend.entity.Asset;
import net.java.hms_backend.entity.Room;
import net.java.hms_backend.exception.AssetException;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.AssetMapper;
import net.java.hms_backend.repository.AssetRepository;
import net.java.hms_backend.repository.RoomRepository;
import net.java.hms_backend.service.AssetService;
import net.java.hms_backend.service.AuditLogService;
import net.java.hms_backend.service.NotificationService;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final RoomRepository roomRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    @Override
    public AssetDto createAsset(AssetDto dto) {
        Integer roomNumber = dto.getRoomNumber();
        if (roomNumber == null) {
            throw new AssetException.NullRoomNumberException("Room number must not be null");
        }

        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "roomNumber", roomNumber));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDateTime now = LocalDateTime.now();
        dto.setCreatedBy(username);
        dto.setCreatedAt(now);

        Asset asset = AssetMapper.toEntity(dto, room);
        Asset saved = assetRepository.save(asset);

        String details = "Created asset with name: " + saved.getName() +
                ", category: " + saved.getCategory() +
                ", condition: " + saved.getCondition() +
                ", originalCost: " + saved.getOriginalCost() +
                ", purchaseDate: " + saved.getPurchaseDate() +
                ", roomNumber: " + roomNumber +
                ", createdBy: " + username +
                ", createdAt: " + now;

        auditLogService.log(
                username,
                "CREATE",
                "Asset",
                saved.getId(),
                details
        );

        return AssetMapper.toDto(saved);
    }

    @Override
    public Page<AssetDto> getAllAssets(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Asset> assetsPage = assetRepository.findAll(pageable);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDateTime timestamp = LocalDateTime.now();

        String details = "Viewed asset list - page: " + page + ", size: " + size +
                ", total assets: " + assetsPage.getTotalElements();

        auditLogService.log(
                username,
                "READ",
                "Asset",
                null,
                details
        );

        return assetsPage.map(AssetMapper::toDto);
    }

    @Override
    public List<AssetDto> getAssetsByRoom(Long roomId) {
        List<Asset> assets = assetRepository.findByRoomId(roomId);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String details = "Viewed assets in roomId: " + roomId +
                ", total assets: " + assets.size();
        auditLogService.log(
                username,
                "READ",
                "Asset",
                null,
                details
        );
        return assets.stream()
                .map(AssetMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public AssetDto updateAsset(Long id, AssetDto dto) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));

        StringBuilder changes = new StringBuilder("Updated asset ID: " + id + ". Changes: ");

        if (dto.getRoomNumber() != null) {
            Room newRoom = roomRepository.findByRoomNumber(dto.getRoomNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", "roomNumber", dto.getRoomNumber()));
            Room oldRoom = asset.getRoom();
            if (oldRoom == null || !oldRoom.getRoomNumber().equals(dto.getRoomNumber())) {
                changes.append("roomNumber: ").append(oldRoom != null ? oldRoom.getRoomNumber() : "null")
                        .append(" → ").append(dto.getRoomNumber()).append("; ");
                asset.setRoom(newRoom);
            }
        }

        if (dto.getName() != null && !dto.getName().equals(asset.getName())) {
            changes.append("name: ").append(asset.getName()).append(" → ").append(dto.getName()).append("; ");
            asset.setName(dto.getName());
        }

        if (dto.getCategory() != null && !dto.getCategory().equals(asset.getCategory())) {
            changes.append("category: ").append(asset.getCategory()).append(" → ").append(dto.getCategory()).append("; ");
            asset.setCategory(dto.getCategory());
        }

        if (dto.getCondition() != null && !dto.getCondition().equals(asset.getCondition())) {
            changes.append("condition: ").append(asset.getCondition()).append(" → ").append(dto.getCondition()).append("; ");
            asset.setCondition(dto.getCondition());
        }

        if (dto.getOriginalCost() != null && !dto.getOriginalCost().equals(asset.getOriginalCost())) {
            changes.append("originalCost: ").append(asset.getOriginalCost()).append(" → ").append(dto.getOriginalCost()).append("; ");
            asset.setOriginalCost(dto.getOriginalCost());
        }

        if (dto.getPurchaseDate() != null && !dto.getPurchaseDate().equals(asset.getPurchaseDate())) {
            changes.append("purchaseDate: ").append(asset.getPurchaseDate()).append(" → ").append(dto.getPurchaseDate()).append("; ");
            asset.setPurchaseDate(dto.getPurchaseDate());
        }

        if (dto.getNote() != null && !dto.getNote().equals(asset.getNote())) {
            changes.append("note: ").append(asset.getNote()).append(" → ").append(dto.getNote()).append("; ");
            asset.setNote(dto.getNote());
        }

        Asset updatedAsset = assetRepository.save(asset);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        auditLogService.log(
                username,
                "UPDATE",
                "Asset",
                updatedAsset.getId(),
                changes.toString()
        );

        return AssetMapper.toDto(updatedAsset);
    }

    @Override
    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String details = "Deleted asset with ID: " + asset.getId() +
                ", name: " + asset.getName() +
                ", category: " + asset.getCategory() +
                ", condition: " + asset.getCondition() +
                ", originalCost: " + asset.getOriginalCost() +
                ", purchaseDate: " + asset.getPurchaseDate();

        auditLogService.log(
                username,
                "DELETE",
                "Asset",
                asset.getId(),
                details
        );

        String title = "Asset Deleted";
        String message = "User " + username + " deleted asset: " + asset.getName() +
                " (ID: " + asset.getId() + ", Category: " + asset.getCategory() + ")";

        notificationService.notifyAdminsAndManagers(
                "ASSET_DELETED",
                title,
                message
        );

        assetRepository.delete(asset);
    }

    @Override
    public Page<AssetDto> searchAssets(AssetFilterRequest filter, int page, int size) {
        List<Asset> assets = assetRepository.findAll();
        Stream<Asset> stream = assets.stream();

        if (filter.getName() != null) {
            stream = stream.filter(a -> a.getName() != null &&
                    a.getName().toLowerCase().contains(filter.getName().toLowerCase()));
        }

        if (filter.getCategory() != null) {
            stream = stream.filter(a -> a.getCategory() != null &&
                    a.getCategory().equalsIgnoreCase(filter.getCategory()));
        }

        if (filter.getCondition() != null) {
            stream = stream.filter(a -> a.getCondition() != null &&
                    a.getCondition().equalsIgnoreCase(filter.getCondition()));
        }

        if (filter.getMinCost() != null) {
            stream = stream.filter(a -> a.getOriginalCost() != null &&
                    a.getOriginalCost() >= filter.getMinCost());
        }

        if (filter.getMaxCost() != null) {
            stream = stream.filter(a -> a.getOriginalCost() != null &&
                    a.getOriginalCost() <= filter.getMaxCost());
        }

        if (filter.getPurchaseDateFrom() != null) {
            stream = stream.filter(a -> a.getPurchaseDate() != null &&
                    !a.getPurchaseDate().isBefore(filter.getPurchaseDateFrom()));
        }

        if (filter.getPurchaseDateTo() != null) {
            stream = stream.filter(a -> a.getPurchaseDate() != null &&
                    !a.getPurchaseDate().isAfter(filter.getPurchaseDateTo()));
        }

        if (filter.getRoomNumber() != null) {
            stream = stream.filter(a -> a.getRoom() != null &&
                    a.getRoom().getRoomNumber() != null &&
                    a.getRoom().getRoomNumber().equals(filter.getRoomNumber()));
        }

        List<Asset> filteredAssets = stream
                .sorted(Comparator.comparing(Asset::getId).reversed())
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        int start = Math.min((int) pageable.getOffset(), filteredAssets.size());
        int end = Math.min(start + pageable.getPageSize(), filteredAssets.size());

        List<Asset> pagedAssets = filteredAssets.subList(start, end);

        Page<Asset> assetsPage = new PageImpl<>(pagedAssets, pageable, filteredAssets.size());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        StringBuilder details = new StringBuilder("Searched assets with filters: ");
        if (filter.getName() != null) details.append("name=").append(filter.getName()).append("; ");
        if (filter.getCategory() != null) details.append("category=").append(filter.getCategory()).append("; ");
        if (filter.getCondition() != null) details.append("condition=").append(filter.getCondition()).append("; ");
        if (filter.getMinCost() != null) details.append("minCost=").append(filter.getMinCost()).append("; ");
        if (filter.getMaxCost() != null) details.append("maxCost=").append(filter.getMaxCost()).append("; ");
        if (filter.getPurchaseDateFrom() != null) details.append("purchaseDateFrom=").append(filter.getPurchaseDateFrom()).append("; ");
        if (filter.getPurchaseDateTo() != null) details.append("purchaseDateTo=").append(filter.getPurchaseDateTo()).append("; ");
        if (filter.getRoomNumber() != null) details.append("roomNumber=").append(filter.getRoomNumber()).append("; ");
        details.append("page=").append(page).append(", size=").append(size)
                .append(", total results=").append(filteredAssets.size());

        auditLogService.log(
                username,
                "SEARCH",
                "Asset",
                null,
                details.toString()
        );

        return assetsPage.map(AssetMapper::toDto);
    }


    @Override
    public AssetDto getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String details = "Viewed asset with ID: " + id +
                ", name: " + asset.getName() +
                ", category: " + asset.getCategory() +
                ", roomNumber: " + (asset.getRoom() != null ? asset.getRoom().getRoomNumber() : "null");

        auditLogService.log(
                username,
                "READ",
                "Asset",
                id,
                details
        );

        return AssetMapper.toDto(asset);
    }

}
