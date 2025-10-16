package com.uade.comedor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.uade.comedor.repository.ReservationRepository;
import com.uade.comedor.entity.Reservation;
import com.uade.comedor.dto.CreateReservationRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;

    public Reservation createReservation(CreateReservationRequest request) {
        Reservation reservation = new Reservation();
        reservation.setUserId(request.getUserId());
        reservation.setLocationId(request.getLocationId());
        reservation.setShift(request.getShift());
        reservation.setStartDateTime(request.getStartDateTime());
        reservation.setEndDateTime(request.getEndDateTime());
        reservation.setStatus(Reservation.ReservationStatus.ACTIVA);
        reservation.setCost(BigDecimal.valueOf(0)); // You might want to calculate this based on your business logic
        reservation.setCreatedAt(LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

    // Get all reservations with optional date filter and pagination
    public List<Reservation> getReservations(LocalDateTime startDate, LocalDateTime endDate, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        if (startDate != null && endDate != null) {
            return reservationRepository.findByStartDateTimeBetween(startDate, endDate, pageable);
        } else if (startDate != null) {
            return reservationRepository.findByStartDateTimeAfter(startDate, pageable);
        } else if (endDate != null) {
            return reservationRepository.findByStartDateTimeBefore(endDate, pageable);
        } else {
            return reservationRepository.findAll(pageable).getContent();
        }
    }

    // Get reservations for a specific user
    public List<Reservation> getReservationsByUser(Long userId) {
        return reservationRepository.findByUserId(userId);
    }
}