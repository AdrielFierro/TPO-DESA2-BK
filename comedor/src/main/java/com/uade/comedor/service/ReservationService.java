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

    @Autowired
    private com.uade.comedor.service.ExternalApiService externalApiService;

    public Reservation createReservation(CreateReservationRequest request) {
        Reservation reservation = new Reservation();
        reservation.setUserId(request.getUserId());
        reservation.setLocationId(request.getLocationId());
        reservation.setShift(request.getShift());
        reservation.setStartDateTime(request.getStartDateTime());
        reservation.setEndDateTime(request.getEndDateTime());
        reservation.setStatus(Reservation.ReservationStatus.ACTIVA);
        reservation.setCost(externalApiService.getReservationCost()); // ahora siempre 25
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

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
    }

    public Reservation cancelReservation(Long id) {
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        r.setStatus(Reservation.ReservationStatus.CANCELADA);
        return reservationRepository.save(r);
    }

    public Reservation confirmReservation(Long id) {
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        if (r.getStatus() == Reservation.ReservationStatus.CANCELADA) {
            throw new IllegalStateException("Cannot confirm a cancelled reservation");
        }
        if (r.getStatus() == Reservation.ReservationStatus.CONFIRMADA) {
            // already confirmed
            return r;
        }
        r.setStatus(Reservation.ReservationStatus.CONFIRMADA);
        return reservationRepository.save(r);
    }

    public BigDecimal getReservationCost(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        return r.getCost();
    }
}