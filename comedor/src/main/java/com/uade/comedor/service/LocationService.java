package com.uade.comedor.service;

import com.uade.comedor.entity.Location;
import com.uade.comedor.entity.MenuMeal;
import com.uade.comedor.entity.MealTimeSlot;
import com.uade.comedor.repository.LocationRepository;
import com.uade.comedor.repository.ReservationRepository;
import com.uade.comedor.dto.TimeSlotAvailabilityDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private MealTimeScheduleService scheduleService;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Autowired
    private ExternalApiService externalApiService;

    /**
     * Obtiene todas las sedes desde el backoffice.
     * En caso de error (por ejemplo en tests), retorna una lista vacía.
     */
    public List<Location> getAllLocations() {
        try {
            return externalApiService.getLocationsFromBackoffice();
        } catch (Exception e) {
            // En tests o si el backoffice no está disponible, retornar lista vacía
            System.err.println("No se pudieron obtener sedes del backoffice: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Location getLocationById(String id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
    }

    public List<TimeSlotAvailabilityDTO> getAvailability(String locationId, MenuMeal.MealTime mealTime, LocalDateTime date) {
        Location location = getLocationById(locationId);
        
        // Obtener los time slots dinámicamente basados en el schedule configurado
        List<MealTimeScheduleService.TimeSlot> timeSlots = scheduleService.calculateTimeSlots(mealTime);
        
        List<TimeSlotAvailabilityDTO> availability = new ArrayList<>();
        
        for (MealTimeScheduleService.TimeSlot slot : timeSlots) {
            // Contar reservas para este horario específico
            long reservedSeats = reservationRepository.countByLocationIdAndMealTimeAndReservationDateBetween(
                locationId,
                mealTime,
                date.toLocalDate().atTime(slot.getStartTime()),
                date.toLocalDate().atTime(slot.getEndTime())
            );
            
            int availableSeats = location.getCapacity() - (int) reservedSeats;
            availability.add(new TimeSlotAvailabilityDTO(
                slot.getStartTime(),
                slot.getEndTime(),
                availableSeats,
                location.getCapacity()
            ));
        }
        
        return availability;
    }
}
