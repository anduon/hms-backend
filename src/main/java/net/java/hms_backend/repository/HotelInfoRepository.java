package net.java.hms_backend.repository;

import net.java.hms_backend.entity.HotelInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotelInfoRepository extends JpaRepository<HotelInfo, Long> {
    Optional<HotelInfo> findTopByOrderByIdAsc();
}

