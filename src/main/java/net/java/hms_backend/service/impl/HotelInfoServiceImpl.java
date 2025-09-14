package net.java.hms_backend.service.impl;

import lombok.AllArgsConstructor;
import net.java.hms_backend.dto.HotelInfoDto;
import net.java.hms_backend.entity.HotelInfo;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.HotelInfoMapper;
import net.java.hms_backend.repository.HotelInfoRepository;
import net.java.hms_backend.service.HotelInfoService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class HotelInfoServiceImpl implements HotelInfoService {

    private final HotelInfoRepository hotelInfoRepository;

    @Override
    public HotelInfoDto getHotelInfo() {
        HotelInfo entity = hotelInfoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("HotelInfo", "id", "any"));
        return HotelInfoMapper.toDto(entity);
    }


    @Override
    public HotelInfoDto updateHotelInfo(HotelInfoDto dto) {
        HotelInfo entity = hotelInfoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("HotelInfo", "id", "any"));

        entity.setName(dto.getName());
        entity.setAddress(dto.getAddress());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setTaxCode(dto.getTaxCode());
        entity.setNumberOfFloors(dto.getNumberOfFloors());
        entity.setCheckInTime(dto.getCheckInTime());
        entity.setCheckOutTime(dto.getCheckOutTime());

        HotelInfo saved = hotelInfoRepository.save(entity);
        return HotelInfoMapper.toDto(saved);
    }

}
