package com.uade.comedor.service;

import com.uade.comedor.entity.Location;
import com.uade.comedor.entity.MenuMeal;
import com.uade.comedor.repository.LocationRepository;
import com.uade.comedor.repository.ReservationRepository;
import com.uade.comedor.dto.TimeSlotAvailabilityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    // Locations are now fetched from backoffice API, no hardcoded initialization needed
    @Autowired
    private ExternalApiService externalApiService;

    /**
     * Obtiene todas las sedes desde el backoffice y las sincroniza con la base de datos local.
     * En caso de error (por ejemplo en tests), retorna las locations de la BD local.
     */
    public List<Location> getAllLocations() {
        try {
            List<Location> locationsFromBackoffice = externalApiService.getLocationsFromBackoffice();
            
            // Sincronizar con la base de datos local
            // Guardar o actualizar cada location
            for (Location location : locationsFromBackoffice) {
                locationRepository.save(location);
            }
            
            return locationsFromBackoffice;
        } catch (Exception e) {
            // En caso de error, retornar las locations de la BD local como fallback
            System.err.println("No se pudieron obtener sedes del backoffice: " + e.getMessage());
            System.err.println("Usando locations de la base de datos local como fallback");
            return locationRepository.findAll();
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
