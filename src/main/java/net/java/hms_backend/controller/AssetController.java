package net.java.hms_backend.controller;

import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.AssetDto;
import net.java.hms_backend.dto.AssetFilterRequest;
import net.java.hms_backend.service.AssetService;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<Page<AssetDto>> getAllAssets(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(assetService.getAllAssets(page, size));
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
    public ResponseEntity<Page<AssetDto>> searchAssets(@RequestBody AssetFilterRequest filter,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(assetService.searchAssets(filter, page, size));
    }

}
