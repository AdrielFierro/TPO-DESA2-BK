package com.uade.comedor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.uade.comedor.repository.ReservationRepository;
import com.uade.comedor.entity.Reservation;
import com.uade.comedor.entity.Location;
import com.uade.comedor.entity.MealTimeSlot;
import com.uade.comedor.dto.CreateReservationRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ExternalApiService externalApiService;
    
    @Autowired
    private MealTimeScheduleService scheduleService;

    public Reservation createReservation(CreateReservationRequest request) {
        LocalTime reservationTime = request.getReservationDate().toLocalTime();
        
        // Calcular los slots disponibles para el mealTime
        List<MealTimeScheduleService.TimeSlot> timeSlots = scheduleService.calculateTimeSlots(request.getMealTime());
        
        // Validar que la hora de la reserva coincida exactamente con el inicio de un slot
        MealTimeScheduleService.TimeSlot targetSlot = null;
        List<LocalTime> validStartTimes = new java.util.ArrayList<>();
        
        for (MealTimeScheduleService.TimeSlot slot : timeSlots) {
            validStartTimes.add(slot.getStartTime());
            if (reservationTime.equals(slot.getStartTime())) {
                targetSlot = slot;
                break;
            }
        }
        
        if (targetSlot == null) {
            String validTimesStr = validStartTimes.stream()
                .map(LocalTime::toString)
                .collect(java.util.stream.Collectors.joining(", "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                String.format("La hora de reserva debe coincidir con el inicio de un slot. Horarios disponibles para %s: %s",
                    request.getMealTime(), validTimesStr));
        }

        // Obtener location y verificar capacidad
        Location location = locationService.getLocationById(request.getLocationId());
        
        // Contar reservas existentes para este slot, location y fecha
        LocalDateTime slotStart = request.getReservationDate().toLocalDate().atTime(targetSlot.getStartTime());
        LocalDateTime slotEnd = request.getReservationDate().toLocalDate().atTime(targetSlot.getEndTime());
        
        long existingReservations = reservationRepository.countByLocationIdAndMealTimeAndReservationDateBetween(
            request.getLocationId(),
            request.getMealTime(),
            slotStart,
            slotEnd
        );

        if (existingReservations >= location.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                String.format("No hay capacidad disponible para %s entre %s y %s en %s el %s. Capacidad máxima: %d",
                    request.getMealTime(),
                    targetSlot.getStartTime(),
                    targetSlot.getEndTime(),
                    location.getName(),
                    request.getReservationDate().toLocalDate(),
                    location.getCapacity()));
        }

        // Determinar el MealTimeSlot enum basado en el slot calculado (para compatibilidad)
        MealTimeSlot timeSlotEnum = determineTimeSlotEnum(request.getMealTime(), targetSlot.getStartTime());

        // Crear reserva
        Reservation reservation = new Reservation();
        reservation.setUserId(request.getUserId());
        reservation.setLocationId(request.getLocationId());
        reservation.setMealTime(request.getMealTime());
        reservation.setReservationTimeSlot(timeSlotEnum);
        reservation.setReservationDate(request.getReservationDate());
        reservation.setStatus(Reservation.ReservationStatus.ACTIVA);
        reservation.setCost(externalApiService.getReservationCost());
        reservation.setCreatedAt(LocalDateTime.now());
        
        return reservationRepository.save(reservation);
    }
    
    // Método helper para determinar el enum MealTimeSlot basado en mealTime y hora de inicio
    private MealTimeSlot determineTimeSlotEnum(com.uade.comedor.entity.MenuMeal.MealTime mealTime, LocalTime startTime) {
        List<MealTimeScheduleService.TimeSlot> slots = scheduleService.calculateTimeSlots(mealTime);
        int slotIndex = 0;
        
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).getStartTime().equals(startTime)) {
                slotIndex = i;
                break;
            }
        }
        
        // Mapear a los enums existentes
        String enumName = mealTime.name() + "_SLOT_" + (slotIndex + 1);
        return MealTimeSlot.valueOf(enumName);
    }

    // Get all reservations
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
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