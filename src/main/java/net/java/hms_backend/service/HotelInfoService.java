package net.java.hms_backend.service;

import net.java.hms_backend.dto.HotelInfoDto;

public interface HotelInfoService {
    HotelInfoDto getHotelInfo();
    HotelInfoDto updateHotelInfo(HotelInfoDto updated);
    Double getWeekendSurchargePercent();
}
