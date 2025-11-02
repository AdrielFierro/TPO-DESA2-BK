package com.uade.comedor.controller;

import com.uade.comedor.entity.MenuMeal;
import com.uade.comedor.dto.MealTimeScheduleDTO;
import com.uade.comedor.service.MealTimeScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/menus/schedules")
public class MealTimeScheduleController {
    
    private final MealTimeScheduleService scheduleService;
    
    public MealTimeScheduleController(MealTimeScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }
    
    @GetMapping
    public ResponseEntity<List<MealTimeScheduleDTO>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }
    
    @GetMapping("/{mealTime}")
    public ResponseEntity<MealTimeScheduleDTO> getSchedule(@PathVariable MenuMeal.MealTime mealTime) {
        return ResponseEntity.ok(scheduleService.getScheduleByMealTime(mealTime));
    }
    
    @PutMapping("/{mealTime}")
    public ResponseEntity<MealTimeScheduleDTO> updateSchedule(
            @PathVariable MenuMeal.MealTime mealTime,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime,
            @RequestParam Integer slotDurationMinutes) {
        return ResponseEntity.ok(scheduleService.updateSchedule(mealTime, startTime, endTime, slotDurationMinutes));
    }
}
