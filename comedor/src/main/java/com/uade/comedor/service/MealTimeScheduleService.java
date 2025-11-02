package com.uade.comedor.service;

import com.uade.comedor.entity.MealTimeSchedule;
import com.uade.comedor.entity.MenuMeal;
import com.uade.comedor.repository.MealTimeScheduleRepository;
import com.uade.comedor.dto.MealTimeScheduleDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        
        while (currentStart.plusMinutes(slotDuration).isBefore(endTime) || 
               currentStart.plusMinutes(slotDuration).equals(endTime)) {
            LocalTime currentEnd = currentStart.plusMinutes(slotDuration);
            slots.add(new TimeSlot(currentStart, currentEnd));
            currentStart = currentEnd;
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
