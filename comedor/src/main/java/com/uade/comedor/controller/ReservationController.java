package com.uade.comedor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.uade.comedor.service.ReservationService;
import com.uade.comedor.entity.Reservation;
import com.uade.comedor.dto.CreateReservationRequest;
import com.uade.comedor.dto.ReservationDateRangeRequest;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.TimeZone;

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

    // GET /reservations - Obtener todas las reservas
    @GetMapping
    public ResponseEntity<List<Reservation>> getReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    // GET /reservations/mine?userId=...
    // Modificado para devolver solo reservas activas y recientes (últimos 2 días)
    @GetMapping("/mine")
    public ResponseEntity<List<Reservation>> getMyReservations(@RequestParam Long userId) {
        List<Reservation> reservations = reservationService.getActiveAndRecentReservationsByUser(userId);
        return ResponseEntity.ok(reservations);
    }
    
    // POST /reservations/mine - Buscar reservas de un usuario entre dos fechas
    @PostMapping("/mine")
    public ResponseEntity<List<Reservation>> searchMyReservationsByDateRange(
            @RequestBody ReservationDateRangeRequest request) {
        List<Reservation> reservations = reservationService.getReservationsByUserAndDateRange(
            request.getUserId(), 
            request.getStartDate(), 
            request.getEndDate()
        );
        return ResponseEntity.ok(reservations);
    }
    
    // GET /reservations/range?startDate=...&endDate=...
    // Obtener todas las reservas de todos los usuarios entre dos fechas
    @GetMapping("/range")
    public ResponseEntity<List<Reservation>> getAllReservationsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Reservation> reservations = reservationService.getAllReservationsByDateRange(startDate, endDate);
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
    @GetMapping("/cost/{reservationId}")
    public ResponseEntity<java.math.BigDecimal> getCost(@PathVariable Long reservationId) {
        try {
            java.math.BigDecimal cost = reservationService.getReservationCost(reservationId);
            return ResponseEntity.ok(cost);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint deshabilitado: Las reservas se confirman automáticamente al confirmar un carrito
    // que tenga la reserva asociada
    /*
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
    */
    
    // GET /reservations/test/timezone - Endpoint de prueba para verificar zona horaria
    @GetMapping("/test/timezone")
    public ResponseEntity<Map<String, String>> getTimezone() {
        Map<String, String> response = new HashMap<>();
        response.put("currentTime", LocalDateTime.now().toString());
        response.put("timezone", TimeZone.getDefault().getID());
        response.put("message", "Hora actual del servidor");
        return ResponseEntity.ok(response);
    }
}
