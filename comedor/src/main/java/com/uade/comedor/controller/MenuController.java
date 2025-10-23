package com.uade.comedor.controller;

import com.uade.comedor.entity.*;
import com.uade.comedor.service.MenuService;
import com.uade.comedor.dto.MenuCreateRequest;
import com.uade.comedor.dto.MenuInputMeal;
import com.uade.comedor.repository.ProductRepository;
import com.uade.comedor.dto.MealShiftDTO;

import java.util.stream.Collectors;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menus")
public class MenuController {
    
    private final MenuService menuService;
    private final ProductRepository productRepository;

    public MenuController(MenuService menuService, ProductRepository productRepository) {
        this.menuService = menuService;
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<List<Menu>> getAllMenus() {
        return ResponseEntity.ok(menuService.getAllMenus());
    }

    @GetMapping("/now")
    public ResponseEntity<Menu> getCurrentMenu() {
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
    public ResponseEntity<Menu> getMenuByDay(@PathVariable Menu.DayOfWeek day) {
        return ResponseEntity.ok(menuService.getMenuByDay(day));
    }

    @PostMapping
    public ResponseEntity<Menu> createMenu(@RequestBody Menu menu) {
        Menu createdMenu = menuService.createMenu(menu);
        return new ResponseEntity<>(createdMenu, HttpStatus.CREATED);
    }

    // Accept DTO with product IDs for convenience
    @PostMapping("/byIds")
    public ResponseEntity<Menu> createMenuByIds(@RequestBody MenuCreateRequest request) {
        Menu menu = new Menu();
        menu.setDay(Menu.DayOfWeek.valueOf(request.getDay()));
        menu.setMeals(new ArrayList<>());

        if (request.getMeals() != null) {
            for (MenuInputMeal input : request.getMeals()) {
                MealBlock mb = new MealBlock();
                mb.setMealTime(MealTime.valueOf(input.getMealTime()));
                mb.setSections(new ArrayList<>());

                // We'll create one MenuSection of type PLATOS and add products by id
                MenuSection platos = new MenuSection();
                platos.setSectionType(MenuSection.SectionType.PLATOS);
                platos.setProducts(
                    input.getProducts().stream()
                        .map(id -> productRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id)))
                        .collect(Collectors.toList())
                );
                mb.getSections().add(platos);
                menu.getMeals().add(mb);
            }
        }

        Menu created = menuService.createMenu(menu);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{day}")
    public ResponseEntity<Menu> updateMenu(@PathVariable Menu.DayOfWeek day, @RequestBody Menu menu) {
        if (menu.getDay() != day) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(menuService.updateMenu(menu));
    }

    @PutMapping("/{day}/byIds")
    public ResponseEntity<Menu> updateMenuByIds(@PathVariable String day, @RequestBody MenuCreateRequest request) {
        Menu menu = new Menu();
        menu.setDay(Menu.DayOfWeek.valueOf(day));
        menu.setMeals(new ArrayList<>());

        if (request.getMeals() != null) {
            for (MenuInputMeal input : request.getMeals()) {
                MealBlock mb = new MealBlock();
                mb.setMealTime(MealTime.valueOf(input.getMealTime()));
                mb.setSections(new ArrayList<>());

                MenuSection platos = new MenuSection();
                platos.setSectionType(MenuSection.SectionType.PLATOS);
                platos.setProducts(
                    input.getProducts().stream()
                        .map(id -> productRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id)))
                        .collect(Collectors.toList())
                );
                mb.getSections().add(platos);
                menu.getMeals().add(mb);
            }
        }

        Menu updated = menuService.updateMenu(menu);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{day}/{mealTime}")
    public ResponseEntity<MealBlock> getMenuByDayAndMealTime(
            @PathVariable Menu.DayOfWeek day,
            @PathVariable MealTime mealTime) {
        return ResponseEntity.ok(menuService.getMenuByDayAndMealTime(day, mealTime));
    }
}
