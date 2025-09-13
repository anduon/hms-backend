package net.java.hms_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.AssetDto;
import net.java.hms_backend.dto.AssetFilterRequest;
import net.java.hms_backend.service.AssetService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<AssetDto> createAsset(@Valid @RequestBody AssetDto dto) {
        return ResponseEntity.ok(assetService.createAsset(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping
    public ResponseEntity<Page<AssetDto>> getAllAssets(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(assetService.getAllAssets(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetDto> getAssetById(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getAssetById(id));
    }


    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<AssetDto>> getAssetsByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(assetService.getAssetsByRoom(roomId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<AssetDto> updateAsset(@PathVariable Long id, @RequestBody AssetDto dto) {
        return ResponseEntity.ok(assetService.updateAsset(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok("Asset deleted successfully.");
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/search")
    public ResponseEntity<Page<AssetDto>> searchAssets(@RequestBody AssetFilterRequest filter,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(assetService.searchAssets(filter, page, size));
    }

}
