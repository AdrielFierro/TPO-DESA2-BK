package com.uade.comedor.controller;

import com.uade.comedor.entity.*;
import com.uade.comedor.service.MenuService;
import com.uade.comedor.dto.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menus")
public class MenuController {
    
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ResponseEntity<MenuResponseDTO> getMenu() {
        return ResponseEntity.ok(menuService.getMenu());
    }

    @GetMapping("/now")
    public ResponseEntity<DayMenuResponseDTO> getCurrentMenu() {
        return ResponseEntity.ok(menuService.getCurrentMenu());
    }

    @GetMapping("/shift")
    public ResponseEntity<List<MealShiftDTO>> getMealShifts() {
        List<MealShiftDTO> shifts = List.of(
            new MealShiftDTO("DESAYUNO", "07:00-12:00"),
            new MealShiftDTO("ALMUERZO", "12:00-16:00"),
            new MealShiftDTO("MERIENDA", "16:00-20:00"),
            new MealShiftDTO("CENA", "20:00-22:00")
        );
        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/{day}")
    public ResponseEntity<DayMenuResponseDTO> getMenuByDay(@PathVariable MenuDay.DayOfWeek day) {
        return ResponseEntity.ok(menuService.getMenuByDay(day));
    }

    @PostMapping
    public ResponseEntity<Object> createMenu(@RequestBody MenuCreateRequest request) {
        return new ResponseEntity<>(menuService.createMenuFromRequest(request), HttpStatus.CREATED);
    }

    // Accept DTO with product IDs for convenience
    @PostMapping("/byIds")
    public ResponseEntity<Object> createMenuByIds(@RequestBody MenuCreateRequest request) {
        return new ResponseEntity<>(menuService.createMenuFromRequest(request), HttpStatus.CREATED);
    }

    @PutMapping("/{day}")
    public ResponseEntity<Object> updateMenu(@PathVariable MenuDay.DayOfWeek day, @RequestBody MenuCreateRequest request) {
        return ResponseEntity.ok(menuService.createMenuFromRequest(request));
    }

    @GetMapping("/{day}/{mealTime}")
    public ResponseEntity<MenuMealResponseDTO> getMenuByDayAndMealTime(
            @PathVariable MenuDay.DayOfWeek day,
            @PathVariable MenuMeal.MealTime mealTime) {
        return ResponseEntity.ok(menuService.getMenuByDayAndMealTime(day, mealTime));
    }

    @PatchMapping("/{day}")
    public ResponseEntity<DayMenuResponseDTO> updateDayMenu(
            @PathVariable MenuDay.DayOfWeek day,
            @RequestBody UpdateDayRequest request) {
        return ResponseEntity.ok(menuService.updateDayMenu(day, request.getMeals()));
    }

    @PatchMapping("/{day}/{mealTime}")
    public ResponseEntity<MenuMealResponseDTO> updateMealMenu(
            @PathVariable MenuDay.DayOfWeek day,
            @PathVariable MenuMeal.MealTime mealTime,
            @RequestBody UpdateMealRequest request) {
        return ResponseEntity.ok(menuService.updateMealMenu(day, mealTime, request.getProductIds()));
    }
}
