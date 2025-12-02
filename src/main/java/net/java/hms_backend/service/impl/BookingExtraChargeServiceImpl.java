package net.java.hms_backend.service.impl;

import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.BookingExtraChargeDto;
import net.java.hms_backend.entity.Booking;
import net.java.hms_backend.entity.BookingExtraCharge;
import net.java.hms_backend.entity.ExtraCharge;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.BookingExtraChargeMapper;
import net.java.hms_backend.repository.BookingExtraChargeRepository;
import net.java.hms_backend.repository.BookingRepository;
import net.java.hms_backend.repository.ExtraChargeRepository;
import net.java.hms_backend.service.BookingExtraChargeService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingExtraChargeServiceImpl implements BookingExtraChargeService {

    private final BookingExtraChargeRepository extraChargeRepo;
    private final BookingRepository bookingRepo;
    private final ExtraChargeRepository chargeRepo;

    @Override
    public BookingExtraChargeDto create(BookingExtraChargeDto dto) {
        Long bookingId = dto.getBookingId();
        Long extraChargeId = dto.getExtraChargeId();

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        ExtraCharge extraCharge = chargeRepo.findById(extraChargeId)
                .orElseThrow(() -> new ResourceNotFoundException("ExtraCharge", "id", extraChargeId));

        BookingExtraCharge entity = BookingExtraChargeMapper.toEntity(dto);
        entity.setBooking(booking);
        entity.setExtraCharge(extraCharge);

        BigDecimal total = extraCharge.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
        entity.setTotalPrice(total);

        BookingExtraCharge saved = extraChargeRepo.save(entity);
        return BookingExtraChargeMapper.toDto(saved);
    }

    @Override
    public BookingExtraChargeDto update(Long id, BookingExtraChargeDto dto) {
        BookingExtraCharge entity = extraChargeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BookingExtraCharge", "id", id));

        if (dto.getQuantity() != entity.getQuantity()) {
            entity.setQuantity(dto.getQuantity());
        }

        if (dto.getNote() != null && !dto.getNote().equals(entity.getNote())) {
            entity.setNote(dto.getNote());
        }

        ExtraCharge extraCharge = entity.getExtraCharge();
        if (dto.getExtraChargeId() != null && !dto.getExtraChargeId().equals(extraCharge.getId())) {
            extraCharge = chargeRepo.findById(dto.getExtraChargeId())
                    .orElseThrow(() -> new ResourceNotFoundException("ExtraCharge", "id", dto.getExtraChargeId()));
            entity.setExtraCharge(extraCharge);
        }

        BigDecimal total = extraCharge.getPrice().multiply(BigDecimal.valueOf(entity.getQuantity()));
        entity.setTotalPrice(total);

        BookingExtraCharge updated = extraChargeRepo.save(entity);
        return BookingExtraChargeMapper.toDto(updated);
    }

    @Override
    public void delete(Long id) {
        BookingExtraCharge entity = extraChargeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BookingExtraCharge", "id", id));
        extraChargeRepo.delete(entity);
    }

    @Override
    public BookingExtraChargeDto getById(Long id) {
        BookingExtraCharge entity = extraChargeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BookingExtraCharge", "id", id));
        return BookingExtraChargeMapper.toDto(entity);
    }

    @Override
    public List<BookingExtraChargeDto> getAll() {
        List<BookingExtraCharge> entities = extraChargeRepo.findAll();
        return entities.stream()
                .map(BookingExtraChargeMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingExtraChargeDto> getByBookingId(Long bookingId) {
        bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        List<BookingExtraCharge> entities = extraChargeRepo.findByBookingId(bookingId);

        return entities.stream()
                .map(BookingExtraChargeMapper::toDto)
                .toList();
    }

}
