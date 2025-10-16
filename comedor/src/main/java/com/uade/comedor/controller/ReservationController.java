package com.uade.comedor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.uade.comedor.service.ReservationService;
import com.uade.comedor.entity.Reservation;
import com.uade.comedor.dto.CreateReservationRequest;


import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody CreateReservationRequest request) {
        Reservation reservation = reservationService.createReservation(request);
        return ResponseEntity.ok(reservation);
    }

    // GET /reservations?startDate=...&endDate=...&page=...&pageSize=...
    @GetMapping
    public ResponseEntity<List<Reservation>> getReservations(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "50") int pageSize) {
        List<Reservation> reservations = reservationService.getReservations(startDate, endDate, page, pageSize);
        return ResponseEntity.ok(reservations);
    }

    // GET /reservations/mine?userId=...
    @GetMapping("/mine")
    public ResponseEntity<List<Reservation>> getMyReservations(@RequestParam Long userId) {
        List<Reservation> reservations = reservationService.getReservationsByUser(userId);
        return ResponseEntity.ok(reservations);
    }

    // GET /reservations/byId/{reservationId}
    @GetMapping("/byreservationId/{reservationId}")
    public ResponseEntity<Reservation> getById(@PathVariable Long reservationId) {
        try {
            Reservation r = reservationService.getReservationById(reservationId);
            return ResponseEntity.ok(r);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /reservations/{reservationId} -> cancel
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Reservation> cancel(@PathVariable Long reservationId) {
        try {
            Reservation r = reservationService.cancelReservation(reservationId);
            return ResponseEntity.ok(r);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /reservations/cost?reservationId=...
    @GetMapping("/cost")
    public ResponseEntity<java.math.BigDecimal> getCost(@RequestParam Long reservationId) {
        try {
            java.math.BigDecimal cost = reservationService.getReservationCost(reservationId);
            return ResponseEntity.ok(cost);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /reservations/confirmation/{id}
    @PostMapping("/confirmation/{id}")
    public ResponseEntity<Reservation> confirm(@PathVariable Long id) {
        try {
            Reservation r = reservationService.confirmReservation(id);
            return ResponseEntity.ok(r);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        }
    }
}
