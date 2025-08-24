package net.java.hms_backend.repository;

import net.java.hms_backend.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByRoomId(Long roomId);
}
