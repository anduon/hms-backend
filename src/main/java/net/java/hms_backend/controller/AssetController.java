package net.java.hms_backend.controller;

import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.AssetDto;
import net.java.hms_backend.dto.AssetFilterRequestDto;
import net.java.hms_backend.service.AssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    public ResponseEntity<AssetDto> createAsset(@RequestBody AssetDto dto) {
        return ResponseEntity.ok(assetService.createAsset(dto));
    }

    @GetMapping
    public ResponseEntity<List<AssetDto>> getAllAssets() {
        return ResponseEntity.ok(assetService.getAllAssets());
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<AssetDto>> getAssetsByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(assetService.getAssetsByRoom(roomId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetDto> updateAsset(@PathVariable Long id, @RequestBody AssetDto dto) {
        return ResponseEntity.ok(assetService.updateAsset(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok("Asset deleted successfully.");
    }

    @PostMapping("/search")
    public ResponseEntity<List<AssetDto>> searchAssets(@RequestBody AssetFilterRequestDto filter) {
        return ResponseEntity.ok(assetService.searchAssets(filter));
    }

}
