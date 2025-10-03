package net.java.hms_backend.service.impl;

import lombok.AllArgsConstructor;
import net.java.hms_backend.dto.HotelInfoDto;
import net.java.hms_backend.entity.HotelInfo;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.HotelInfoMapper;
import net.java.hms_backend.repository.HotelInfoRepository;
import net.java.hms_backend.service.AuditLogService;
import net.java.hms_backend.service.HotelInfoService;
import net.java.hms_backend.service.NotificationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class HotelInfoServiceImpl implements HotelInfoService {

    private final HotelInfoRepository hotelInfoRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    @Override
    public HotelInfoDto getHotelInfo() {
        HotelInfo entity = hotelInfoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("HotelInfo", "id", "any"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String details = "Viewed hotel info: name=" + entity.getName() +
                ", address=" + entity.getAddress() +
                ", phone=" + entity.getPhone() +
                ", email=" + entity.getEmail() +
                ", taxCode=" + entity.getTaxCode() +
                ", numberOfFloors=" + entity.getNumberOfFloors() +
                ", checkInTime=" + entity.getCheckInTime() +
                ", checkOutTime=" + entity.getCheckOutTime();

        auditLogService.log(
                username,
                "READ",
                "HotelInfo",
                entity.getId(),
                details
        );
        return HotelInfoMapper.toDto(entity);
    }


    @Override
    public HotelInfoDto updateHotelInfo(HotelInfoDto dto) {
        HotelInfo entity = hotelInfoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("HotelInfo", "id", "any"));

        StringBuilder changes = new StringBuilder("Updated HotelInfo ID: " + entity.getId() + ". Changes: ");

        if (dto.getName() != null && !dto.getName().equals(entity.getName())) {
            changes.append("name: ").append(entity.getName()).append(" → ").append(dto.getName()).append("; ");
            entity.setName(dto.getName());
        }

        if (dto.getAddress() != null && !dto.getAddress().equals(entity.getAddress())) {
            changes.append("address: ").append(entity.getAddress()).append(" → ").append(dto.getAddress()).append("; ");
            entity.setAddress(dto.getAddress());
        }

        if (dto.getPhone() != null && !dto.getPhone().equals(entity.getPhone())) {
            changes.append("phone: ").append(entity.getPhone()).append(" → ").append(dto.getPhone()).append("; ");
            entity.setPhone(dto.getPhone());
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(entity.getEmail())) {
            changes.append("email: ").append(entity.getEmail()).append(" → ").append(dto.getEmail()).append("; ");
            entity.setEmail(dto.getEmail());
        }

        if (dto.getTaxCode() != null && !dto.getTaxCode().equals(entity.getTaxCode())) {
            changes.append("taxCode: ").append(entity.getTaxCode()).append(" → ").append(dto.getTaxCode()).append("; ");
            entity.setTaxCode(dto.getTaxCode());
        }

        if (dto.getNumberOfFloors() != null && !dto.getNumberOfFloors().equals(entity.getNumberOfFloors())) {
            changes.append("numberOfFloors: ").append(entity.getNumberOfFloors()).append(" → ").append(dto.getNumberOfFloors()).append("; ");
            entity.setNumberOfFloors(dto.getNumberOfFloors());
        }

        if (dto.getCheckInTime() != null && !dto.getCheckInTime().equals(entity.getCheckInTime())) {
            changes.append("checkInTime: ").append(entity.getCheckInTime()).append(" → ").append(dto.getCheckInTime()).append("; ");
            entity.setCheckInTime(dto.getCheckInTime());
        }

        if (dto.getCheckOutTime() != null && !dto.getCheckOutTime().equals(entity.getCheckOutTime())) {
            changes.append("checkOutTime: ").append(entity.getCheckOutTime()).append(" → ").append(dto.getCheckOutTime()).append("; ");
            entity.setCheckOutTime(dto.getCheckOutTime());
        }

        HotelInfo saved = hotelInfoRepository.save(entity);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        auditLogService.log(
                username,
                "UPDATE",
                "HotelInfo",
                saved.getId(),
                changes.toString()
        );

        String title = "Hotel Information Updated";
        String message = "User " + username + " updated hotel information. Changes: " + changes;

        notificationService.notifyAdminsAndManagers(
                "HOTEL_INFO_UPDATED",
                title,
                message
        );

        return HotelInfoMapper.toDto(saved);
    }
}
