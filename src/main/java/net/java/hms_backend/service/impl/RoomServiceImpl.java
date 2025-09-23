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
import net.java.hms_backend.service.PromotionService;
import net.java.hms_backend.service.RoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;



@Service
@AllArgsConstructor
public class RoomServiceImpl implements RoomService {

    private RoomRepository roomRepository;
    private final PromotionService promotionService;

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
        return RoomMapper.mapToRoomDto(savedRoom);
    }


    @Override
    public Page<RoomDto> getAllRooms(int page, int size) {
        Optional<Promotion> promotionOpt = promotionService.getActivePromotion();

        Pageable pageable = PageRequest.of(page, size);
        Page<Room> roomsPage = roomRepository.findAll(pageable);

        return roomsPage.map(room -> RoomMapper.mapToRoomDto(room, promotionOpt));
    }



    @Override
    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        return RoomMapper.mapToRoomDto(room);
    }

    @Override
    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));

        if (roomDto.getRoomNumber() != null) {
            if (roomDto.getRoomNumber().equals(room.getRoomNumber())) {
            } else if (roomRepository.existsByRoomNumber(roomDto.getRoomNumber())) {
                throw new RoomException.DuplicateRoomException("Room number already exists: " + roomDto.getRoomNumber());
            } else {
                room.setRoomNumber(roomDto.getRoomNumber());
            }
        }

        if (roomDto.getMaxOccupancy() != null) {
            room.setMaxOccupancy(roomDto.getMaxOccupancy());
        }

        if (roomDto.getRoomType() != null) {
            room.setRoomType(roomDto.getRoomType());
        }

        if (roomDto.getStatus() != null) {
            room.setStatus(roomDto.getStatus());
        }

        if (roomDto.getLocation() != null) {
            room.setLocation(roomDto.getLocation());
        }

        if (roomDto.getPrices() != null) {
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
        return RoomMapper.mapToRoomDto(updatedRoom);
    }


    @Override
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
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

        Pageable pageable = PageRequest.of(page, size);

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

        Optional<Promotion> promotionOpt = promotionService.getActivePromotion();
        return roomsPage.map(room -> RoomMapper.mapToRoomDto(room, promotionOpt));
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
                    cb.greaterThanOrEqualTo(booking.get("checkOutDate"), filter.getDesiredCheckIn()),
                    cb.notEqual(booking.get("status"), "CANCELLED")
            );

            subquery.where(overlap);
            predicates.add(cb.not(cb.exists(subquery)));
        }

        return predicates;
    }

}
