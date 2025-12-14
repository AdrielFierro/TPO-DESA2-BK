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

    @Autowired
    private ReservationEventService reservationEventService;

    public Reservation createReservation(CreateReservationRequest request) {
        LocalTime reservationTime = request.getReservationDate().toLocalTime();
        
        // Validar que la fecha de la reserva no sea en el pasado
        LocalDateTime now = LocalDateTime.now();
        if (request.getReservationDate().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("No puedes crear una reserva para una fecha pasada. Fecha solicitada: %s. Fecha/hora actual: %s",
                    request.getReservationDate(), now));
        }
        
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
        
        // Verificar si el usuario ya tiene una reserva en este slot
        long userExistingReservations = reservationRepository.countByUserIdAndMealTimeAndReservationDateBetween(
            request.getUserId(),
            request.getMealTime(),
            slotStart,
            slotEnd
        );
        
        if (userExistingReservations > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                String.format("Ya tienes una reserva para %s entre %s y %s el %s. Solo puedes tener una reserva por bloque horario.",
                    request.getMealTime(),
                    targetSlot.getStartTime(),
                    targetSlot.getEndTime(),
                    request.getReservationDate().toLocalDate()));
        }
        
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
        
    // Guardar horarios explícitos del slot para el frontend
    reservation.setSlotStartTime(targetSlot.getStartTime());
    reservation.setSlotEndTime(targetSlot.getEndTime());
        
    Reservation savedReservation = reservationRepository.save(reservation);
    
    // Publicar evento de reserva creada
    reservationEventService.publishReservationCreatedEvent(savedReservation);
    
    return savedReservation;
    }
    
    // Método helper para determinar el enum MealTimeSlot basado en mealTime y hora de inicio
    // Retorna null si no existe el enum (para slots dinámicos que exceden los predefinidos)
    private MealTimeSlot determineTimeSlotEnum(com.uade.comedor.entity.MenuMeal.MealTime mealTime, LocalTime startTime) {
        List<MealTimeScheduleService.TimeSlot> slots = scheduleService.calculateTimeSlots(mealTime);
        int slotIndex = 0;
        
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).getStartTime().equals(startTime)) {
                slotIndex = i;
                break;
            }
        }
        
        // Intentar mapear a los enums existentes, retornar null si no existe
        String enumName = mealTime.name() + "_SLOT_" + (slotIndex + 1);
        try {
            return MealTimeSlot.valueOf(enumName);
        } catch (IllegalArgumentException e) {
            // El enum no existe, retornar null (slots dinámicos adicionales)
            return null;
        }
    }

    // Get all reservations
    public List<Reservation> getAllReservations() {
        List<Reservation> list = reservationRepository.findAll();
        list.forEach(this::populateSlotTimes);
        return list;
    }

    // Get reservations for a specific user
    public List<Reservation> getReservationsByUser(String userId) {
        List<Reservation> list = reservationRepository.findByUserId(userId);
        list.forEach(this::populateSlotTimes);
        return list;
    }

    public Reservation getReservationById(Long id) {
        Reservation r = reservationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        
        // Validar según el estado de la reserva
        if (r.getStatus() == Reservation.ReservationStatus.AUSENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "La reserva está vencida");
        }
        
        if (r.getStatus() == Reservation.ReservationStatus.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "La reserva fue cancelada por el usuario");
        }
        
        // Validar si está CONFIRMADA pero ya pasó el horario de fin
        if (r.getStatus() == Reservation.ReservationStatus.CONFIRMADA) {
            // Asegurar que los horarios del slot estén poblados
            populateSlotTimes(r);
            
            if (r.getSlotEndTime() != null) {
                // Combinar la fecha de la reserva con el horario de fin del slot
                LocalDateTime reservationEndDateTime = r.getReservationDate().toLocalDate()
                    .atTime(r.getSlotEndTime());
                LocalDateTime now = LocalDateTime.now();
                
                // Si la hora actual es igual o posterior al fin del slot, la reserva ya finalizó
                if (!now.isBefore(reservationEndDateTime)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La reserva ya finalizó");
                }
            }
        }
        
        // Estados válidos: ACTIVA y CONFIRMADA (dentro del horario) permiten continuar
        populateSlotTimes(r);
        return r;
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
        
        // Validar que la confirmación se haga dentro de la ventana de tiempo permitida
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationTime = r.getReservationDate();
        LocalDateTime earliestConfirmTime = reservationTime.minusMinutes(20);
        LocalDateTime latestConfirmTime = reservationTime.plusMinutes(20);
        
        if (now.isBefore(earliestConfirmTime) || now.isAfter(latestConfirmTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("Solo puedes confirmar la reserva entre 20 minutos antes y 20 minutos después del horario reservado. " +
                             "Horario de reserva: %s. Ventana de confirmación: %s a %s. Hora actual: %s",
                             reservationTime,
                             earliestConfirmTime,
                             latestConfirmTime,
                             now));
        }
        
        r.setStatus(Reservation.ReservationStatus.CONFIRMADA);
        return reservationRepository.save(r);
    }

    public BigDecimal getReservationCost(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        return r.getCost();
    }

    /**
     * Obtiene el costo que tendrá una nueva reserva.
     * Llama al endpoint externo del módulo de Backoffice para obtener el costo dinámico.
     * Si falla, retorna 25 como fallback.
     */
    public BigDecimal getNextReservationCost() {
        return externalApiService.getReservationCost();
    }
    
    /**
     * Obtiene las reservas de un usuario:
     * - Todas las ACTIVAS y CONFIRMADAS (sin límite de tiempo)
     * - AUSENTES de los últimos 2 días
     * - NO muestra CANCELADAS
     * 
     * Nota: Las reservas ACTIVAS vencidas se marcan como AUSENTE automáticamente
     * por una tarea programada que se ejecuta cada 15 minutos
     */
    public List<Reservation> getActiveAndRecentReservationsByUser(String userId) {
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        List<Reservation> list = reservationRepository.findActiveAndRecentByUserId(userId, twoDaysAgo);
        list.forEach(this::populateSlotTimes);
        return list;
    }
    
    /**
     * Obtiene las reservas de un usuario entre dos fechas
     */
    public List<Reservation> getReservationsByUserAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Reservation> list = reservationRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        list.forEach(this::populateSlotTimes);
        return list;
    }
    
    /**
     * Obtiene todas las reservas de todos los usuarios entre dos fechas
     */
    public List<Reservation> getAllReservationsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Reservation> list = reservationRepository.findByDateBetween(startDate, endDate);
        list.forEach(this::populateSlotTimes);
        return list;
    }

    // Helper: asegura que slotStartTime/slotEndTime estén poblados en la entidad
    private void populateSlotTimes(Reservation r) {
        if (r == null) return;

        // Intentar derivar siempre desde el schedule usando la hora de reservationDate
        try {
            List<MealTimeScheduleService.TimeSlot> slots = scheduleService.calculateTimeSlots(r.getMealTime());
            java.time.LocalTime time = r.getReservationDate().toLocalTime();
            for (MealTimeScheduleService.TimeSlot s : slots) {
                if (s.getStartTime().equals(time)) {
                    r.setSlotStartTime(s.getStartTime());
                    r.setSlotEndTime(s.getEndTime());
                    return;
                }
            }
        } catch (Exception e) {
            // si falla, intentaremos con el enum o con los valores almacenados
        }

        // Si no se pudo derivar del schedule, usar reservationTimeSlot (enum) si existe
        MealTimeSlot enumSlot = r.getReservationTimeSlot();
        if (enumSlot != null) {
            r.setSlotStartTime(enumSlot.getStartTime());
            r.setSlotEndTime(enumSlot.getEndTime());
            return;
        }

        // Finalmente, si hay valores almacenados en la entidad, mantenerlos (no sobrescribir)
        // Si tampoco existen, quedarán como null
    }
}