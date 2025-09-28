package net.java.hms_backend.service.impl;

import lombok.AllArgsConstructor;
import net.java.hms_backend.dto.RoomDto;
import net.java.hms_backend.dto.RoomFilterRequest;
import net.java.hms_backend.dto.RoomPriceDto;
import net.java.hms_backend.entity.Booking;
import net.java.hms_backend.entity.Promotion;
import net.java.hms_backend.entity.Room;
import net.java.hms_backend.entity.RoomPrice;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.exception.RoomException;
import net.java.hms_backend.mapper.RoomMapper;
import net.java.hms_backend.repository.RoomRepository;
import net.java.hms_backend.service.AuditLogService;
import net.java.hms_backend.service.PromotionService;
import net.java.hms_backend.service.RoomService;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;



@Service
@AllArgsConstructor
public class RoomServiceImpl implements RoomService {

    private RoomRepository roomRepository;
    private final PromotionService promotionService;
    private final AuditLogService auditLogService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public RoomDto createRoom(RoomDto roomDto) {
        if (roomDto.getRoomNumber() == null) {
            throw new RoomException.NullRoomNumberException("Room number must not be null");
        }

        if (roomRepository.existsByRoomNumber(roomDto.getRoomNumber())) {
            throw new RoomException.DuplicateRoomException("Room number already exists: " + roomDto.getRoomNumber());
        }
        Room room = RoomMapper.mapToRoom(roomDto);
        if (room.getPrices() != null) {
            room.getPrices().forEach(price -> price.setRoom(room));
        }
        Room savedRoom = roomRepository.save(room);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        StringBuilder details = new StringBuilder("Created room: [ID=")
                .append(savedRoom.getId())
                .append(", RoomNumber=").append(savedRoom.getRoomNumber())
                .append(", Type=").append(savedRoom.getRoomType())
                .append(", Status=").append(savedRoom.getStatus())
                .append(", Location=").append(savedRoom.getLocation())
                .append(", MaxOccupancy=").append(savedRoom.getMaxOccupancy());

        if (savedRoom.getPrices() != null && !savedRoom.getPrices().isEmpty()) {
            details.append(", Prices=").append(savedRoom.getPrices().size()).append(" types");
        }

        details.append("]");
        auditLogService.log(
                username,
                "CREATE",
                "Room",
                savedRoom.getId(),
                details.toString()
        );

        return RoomMapper.mapToRoomDto(savedRoom);
    }

    @Override
    public Page<RoomDto> getAllRooms(int page, int size) {
        Optional<Promotion> promotionOpt = promotionService.getActivePromotion();
        Pageable pageable = PageRequest.of(page, size, Sort.by("roomNumber").ascending());
        Page<Room> roomsPage = roomRepository.findAll(pageable);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String details = "Retrieved room list - Page: " + page +
                ", Size: " + size +
                ", Total: " + roomsPage.getTotalElements();

        auditLogService.log(
                username,
                "QUERY",
                "Room",
                null,
                details
        );
        return roomsPage.map(room -> RoomMapper.mapToRoomDto(room, promotionOpt));
    }

    @Override
    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));

        Optional<Promotion> promotionOpt = promotionService.getActivePromotion();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String details = "Retrieved room by ID: " + id +
                ", RoomNumber: " + room.getRoomNumber() +
                ", Type: " + room.getRoomType() +
                ", Status: " + room.getStatus() +
                ", Location: " + room.getLocation();

        auditLogService.log(
                username,
                "QUERY",
                "Room",
                room.getId(),
                details
        );
        return RoomMapper.mapToRoomDto(room, promotionOpt);
    }

    @Override
    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));

        StringBuilder changes = new StringBuilder("Updated room ID: ").append(id).append(". Changes: ");

        if (roomDto.getRoomNumber() != null) {
            if (!roomDto.getRoomNumber().equals(room.getRoomNumber())) {
                if (roomRepository.existsByRoomNumber(roomDto.getRoomNumber())) {
                    throw new RoomException.DuplicateRoomException("Room number already exists: " + roomDto.getRoomNumber());
                }
                changes.append("roomNumber: ").append(room.getRoomNumber())
                        .append(" → ").append(roomDto.getRoomNumber()).append("; ");
                room.setRoomNumber(roomDto.getRoomNumber());
            }
        }

        if (roomDto.getMaxOccupancy() != null && !roomDto.getMaxOccupancy().equals(room.getMaxOccupancy())) {
            changes.append("maxOccupancy: ").append(room.getMaxOccupancy())
                    .append(" → ").append(roomDto.getMaxOccupancy()).append("; ");
            room.setMaxOccupancy(roomDto.getMaxOccupancy());
        }

        if (roomDto.getRoomType() != null && !roomDto.getRoomType().equals(room.getRoomType())) {
            changes.append("roomType: ").append(room.getRoomType())
                    .append(" → ").append(roomDto.getRoomType()).append("; ");
            room.setRoomType(roomDto.getRoomType());
        }

        if (roomDto.getStatus() != null && !roomDto.getStatus().equals(room.getStatus())) {
            changes.append("status: ").append(room.getStatus())
                    .append(" → ").append(roomDto.getStatus()).append("; ");
            room.setStatus(roomDto.getStatus());
        }

        if (roomDto.getLocation() != null && !roomDto.getLocation().equals(room.getLocation())) {
            changes.append("location: ").append(room.getLocation())
                    .append(" → ").append(roomDto.getLocation()).append("; ");
            room.setLocation(roomDto.getLocation());
        }

        if (roomDto.getPrices() != null) {
            changes.append("prices updated; ");
            room.getPrices().clear();
            for (RoomPriceDto dto : roomDto.getPrices()) {
                RoomPrice price = new RoomPrice();
                price.setRoom(room);
                price.setPriceType(dto.getPriceType());
                price.setBasePrice(dto.getBasePrice());
                room.getPrices().add(price);
            }
        }

        Room updatedRoom = roomRepository.save(room);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        auditLogService.log(
                username,
                "UPDATE",
                "Room",
                updatedRoom.getId(),
                changes.toString()
        );

        return RoomMapper.mapToRoomDto(updatedRoom);
    }


    @Override
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String details = "Deleted room: [ID=" + room.getId() +
                ", RoomNumber=" + room.getRoomNumber() +
                ", Type=" + room.getRoomType() +
                ", Status=" + room.getStatus() +
                ", Location=" + room.getLocation() +
                ", MaxOccupancy=" + room.getMaxOccupancy() + "]";

        auditLogService.log(
                username,
                "DELETE",
                "Room",
                room.getId(),
                details
        );
        roomRepository.delete(room);
    }

    @Override
    public Page<RoomDto> filterRooms(RoomFilterRequest filter, int page, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Room> query = cb.createQuery(Room.class);
        Root<Room> roomRoot = query.from(Room.class);
        query.select(roomRoot).distinct(true);

        List<Predicate> predicates = buildRoomPredicates(filter, cb, roomRoot, query);
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        Pageable pageable = PageRequest.of(page, size, Sort.by("roomNumber").ascending());
        List<Room> result = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Room> countRoot = countQuery.from(Room.class);
        countQuery.select(cb.countDistinct(countRoot));

        List<Predicate> countPredicates = buildRoomPredicates(filter, cb, countRoot, countQuery);
        countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        Page<Room> roomsPage = new PageImpl<>(result, pageable, total);

        Optional<Promotion> promotionOpt;
        if (filter.getDesiredCheckIn() != null && filter.getDesiredCheckOut() != null) {
            promotionOpt = promotionService.getPromotionForBooking(
                    filter.getDesiredCheckIn(),
                    filter.getDesiredCheckOut()
            );
        } else {
            promotionOpt = Optional.empty();
        }
        Optional<Promotion> finalPromotionOpt = promotionOpt;

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        StringBuilder details = new StringBuilder("Filtered rooms with criteria: ");

        if (filter.getRoomType() != null) {
            details.append("roomType=").append(filter.getRoomType()).append("; ");
        }
        if (filter.getStatus() != null) {
            details.append("status=").append(filter.getStatus()).append("; ");
        }
        if (filter.getLocation() != null) {
            details.append("location=").append(filter.getLocation()).append("; ");
        }
        if (filter.getMaxOccupancy() != null) {
            details.append("maxOccupancy>=").append(filter.getMaxOccupancy()).append("; ");
        }
        if (filter.getDesiredCheckIn() != null && filter.getDesiredCheckOut() != null) {
            details.append("desiredCheckIn=").append(filter.getDesiredCheckIn()).append("; ");
            details.append("desiredCheckOut=").append(filter.getDesiredCheckOut()).append("; ");
        }

        details.append("Page=").append(page)
                .append(", Size=").append(size)
                .append(", TotalResults=").append(total);

        auditLogService.log(
                username,
                "FILTER",
                "Room",
                null,
                details.toString()
        );
        return roomsPage.map(room -> RoomMapper.mapToRoomDto(room, finalPromotionOpt));
    }


    private List<Predicate> buildRoomPredicates(RoomFilterRequest filter, CriteriaBuilder cb, Root<Room> root, CriteriaQuery<?> query) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getRoomType() != null && !filter.getRoomType().isBlank()) {
            predicates.add(cb.equal(root.get("roomType"), filter.getRoomType()));
        }

        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }

        if (filter.getLocation() != null && !filter.getLocation().isBlank()) {
            predicates.add(cb.equal(root.get("location"), filter.getLocation()));
        }

        if (filter.getMaxOccupancy() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("maxOccupancy"), filter.getMaxOccupancy()));
        }

        if (filter.getDesiredCheckIn() != null && filter.getDesiredCheckOut() != null) {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Booking> booking = subquery.from(Booking.class);
            Join<Booking, Room> bookingRoom = booking.join("room");

            subquery.select(cb.literal(1L));

            Predicate overlap = cb.and(
                    cb.equal(bookingRoom, root),
                    cb.lessThanOrEqualTo(booking.get("checkInDate"), filter.getDesiredCheckOut()),
                    cb.greaterThanOrEqualTo(
                            cb.<LocalDateTime>selectCase()
                                    .when(cb.isNotNull(booking.get("actualCheckOutTime")), booking.get("actualCheckOutTime"))
                                    .otherwise(booking.get("checkOutDate")),
                            filter.getDesiredCheckIn()
                    ),
                    cb.notEqual(booking.get("status"), "CANCELLED")
            );


            subquery.where(overlap);
            predicates.add(cb.not(cb.exists(subquery)));
        }

        return predicates;
    }
}
