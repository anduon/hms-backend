package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.BookingExtraChargeDto;
import net.java.hms_backend.entity.BookingExtraCharge;

public class BookingExtraChargeMapper {

    public static BookingExtraChargeDto toDto(BookingExtraCharge entity) {
        if (entity == null) {
            return null;
        }

        return new BookingExtraChargeDto(
                entity.getId(),
                entity.getBooking() != null ? entity.getBooking().getId() : null,
                entity.getExtraCharge() != null ? entity.getExtraCharge().getId() : null,
                entity.getQuantity(),
                entity.getTotalPrice(),
                entity.getNote()
        );
    }

    public static BookingExtraCharge toEntity(BookingExtraChargeDto dto) {
        if (dto == null) {
            return null;
        }

        BookingExtraCharge entity = new BookingExtraCharge();
        entity.setId(dto.getId());

        entity.setQuantity(dto.getQuantity());
        entity.setTotalPrice(dto.getTotalPrice());
        entity.setNote(dto.getNote());

        return entity;
    }
}
