package com.uade.comedor.service;

import com.uade.comedor.entity.MealTimeSchedule;
import com.uade.comedor.entity.MenuMeal;
import com.uade.comedor.repository.MealTimeScheduleRepository;
import com.uade.comedor.dto.MealTimeScheduleDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MealTimeScheduleService {
    
    @Autowired
    private MealTimeScheduleRepository scheduleRepository;
    
    @PostConstruct
    @Transactional
    public void initializeSchedules() {
        // Inicializar horarios por defecto si no existen
        if (scheduleRepository.count() == 0) {
            scheduleRepository.save(new MealTimeSchedule(
                MenuMeal.MealTime.DESAYUNO,
                LocalTime.of(7, 0),
                LocalTime.of(9, 0),
                60
            ));
            
            scheduleRepository.save(new MealTimeSchedule(
                MenuMeal.MealTime.ALMUERZO,
                LocalTime.of(12, 0),
                LocalTime.of(14, 0),
                60
            ));
            
            scheduleRepository.save(new MealTimeSchedule(
                MenuMeal.MealTime.MERIENDA,
                LocalTime.of(16, 0),
                LocalTime.of(18, 0),
                60
            ));
            
            scheduleRepository.save(new MealTimeSchedule(
                MenuMeal.MealTime.CENA,
                LocalTime.of(20, 0),
                LocalTime.of(22, 0),
                60
            ));
        }
    }
    
    public List<MealTimeScheduleDTO> getAllSchedules() {
        return scheduleRepository.findAll().stream()
            .map(schedule -> new MealTimeScheduleDTO(
                schedule.getMealTime(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getSlotDurationMinutes()
            ))
            .collect(Collectors.toList());
    }
    
    public MealTimeScheduleDTO getScheduleByMealTime(MenuMeal.MealTime mealTime) {
        MealTimeSchedule schedule = scheduleRepository.findByMealTime(mealTime)
            .orElseThrow(() -> new RuntimeException("Schedule not found for meal time: " + mealTime));
        
        return new MealTimeScheduleDTO(
            schedule.getMealTime(),
            schedule.getStartTime(),
            schedule.getEndTime(),
            schedule.getSlotDurationMinutes()
        );
    }
    
    public MealTimeSchedule getScheduleEntity(MenuMeal.MealTime mealTime) {
        return scheduleRepository.findByMealTime(mealTime)
            .orElseThrow(() -> new RuntimeException("Schedule not found for meal time: " + mealTime));
    }
    
    @Transactional
    public MealTimeScheduleDTO updateSchedule(MenuMeal.MealTime mealTime, LocalTime startTime, LocalTime endTime, Integer slotDurationMinutes) {
        MealTimeSchedule schedule = scheduleRepository.findByMealTime(mealTime)
            .orElseThrow(() -> new RuntimeException("Schedule not found for meal time: " + mealTime));
        
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setSlotDurationMinutes(slotDurationMinutes);
        
        MealTimeSchedule saved = scheduleRepository.save(schedule);
        
        return new MealTimeScheduleDTO(
            saved.getMealTime(),
            saved.getStartTime(),
            saved.getEndTime(),
            saved.getSlotDurationMinutes()
        );
    }
    
    // Calcula los time slots dinámicamente basados en el schedule
    public List<TimeSlot> calculateTimeSlots(MenuMeal.MealTime mealTime) {
        MealTimeSchedule schedule = getScheduleEntity(mealTime);
        
        List<TimeSlot> slots = new java.util.ArrayList<>();
        LocalTime currentStart = schedule.getStartTime();
        LocalTime endTime = schedule.getEndTime();
        int slotDuration = schedule.getSlotDurationMinutes();
        
        // Detectar si cruza medianoche (end < start)
        boolean crossesMidnight = endTime.isBefore(currentStart) || endTime.equals(LocalTime.MIDNIGHT);
        
        // Usar LocalDateTime para manejar correctamente el cruce de medianoche
        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), currentStart);
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), endTime);
        
        if (crossesMidnight && !endTime.equals(LocalTime.MIDNIGHT)) {
            // Si end < start, mover end al día siguiente
            endDateTime = endDateTime.plusDays(1);
        } else if (endTime.equals(LocalTime.MIDNIGHT)) {
            // Medianoche del día siguiente
            endDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);
        }
        
        LocalDateTime current = startDateTime;
        
        // Generar slots con protección contra bucle infinito
        int maxSlots = 100; // Protección: máximo 100 slots
        int slotCount = 0;
        
        while ((current.plusMinutes(slotDuration).isBefore(endDateTime) || 
                current.plusMinutes(slotDuration).equals(endDateTime)) && 
               slotCount < maxSlots) {
            LocalTime slotStart = current.toLocalTime();
            LocalTime slotEnd = current.plusMinutes(slotDuration).toLocalTime();
            slots.add(new TimeSlot(slotStart, slotEnd));
            current = current.plusMinutes(slotDuration);
            slotCount++;
        }
        
        if (slotCount >= maxSlots) {
            throw new IllegalStateException(
                String.format("Se alcanzó el límite de slots (%d) para %s. Verifica la configuración: start=%s, end=%s, duration=%d min",
                    maxSlots, mealTime, currentStart, endTime, slotDuration));
        }
        
        return slots;
    }
    
    // Clase interna para representar un slot de tiempo
    public static class TimeSlot {
        private final LocalTime startTime;
        private final LocalTime endTime;
        
        public TimeSlot(LocalTime startTime, LocalTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        public LocalTime getStartTime() {
            return startTime;
        }
        
        public LocalTime getEndTime() {
            return endTime;
        }
    }
}
