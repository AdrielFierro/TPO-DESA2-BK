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

    @PostConstruct
    @Transactional
    public void initializeLocations() {
        // Inicializar locations hardcodeadas si no existen
        if (locationRepository.count() == 0) {
            Location norte = new Location();
            norte.setName("Norte");
            norte.setCapacity(10);
            locationRepository.save(norte);

            Location sur = new Location();
            sur.setName("Sur");
            sur.setCapacity(10);
            locationRepository.save(sur);
        }
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Location getLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
    }

    public List<TimeSlotAvailabilityDTO> getAvailability(Long locationId, MenuMeal.MealTime mealTime, LocalDateTime date) {
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
