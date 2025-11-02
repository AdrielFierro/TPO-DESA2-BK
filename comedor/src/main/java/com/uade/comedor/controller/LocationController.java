package com.uade.comedor.controller;

import com.uade.comedor.entity.Location;
import com.uade.comedor.entity.MenuMeal;
import com.uade.comedor.dto.TimeSlotAvailabilityDTO;
import com.uade.comedor.service.LocationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<List<TimeSlotAvailabilityDTO>> getAvailability(
            @PathVariable Long id,
            @RequestParam MenuMeal.MealTime mealTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return ResponseEntity.ok(locationService.getAvailability(id, mealTime, date));
    }
}
