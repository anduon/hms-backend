package net.java.hms_backend.repository;

import net.java.hms_backend.entity.HotelInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelInfoRepository extends JpaRepository<HotelInfo, Long> {
}

