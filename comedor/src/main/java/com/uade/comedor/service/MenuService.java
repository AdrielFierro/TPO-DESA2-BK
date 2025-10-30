package com.uade.comedor.service;

import com.uade.comedor.dto.*;
import com.uade.comedor.entity.*;
import com.uade.comedor.repository.MenuRepository;
import com.uade.comedor.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final ProductRepository productRepository;
    private final MenuDTOMapper menuDTOMapper;

    public MenuService(MenuRepository menuRepository, ProductRepository productRepository, MenuDTOMapper menuDTOMapper) {
        this.menuRepository = menuRepository;
        this.productRepository = productRepository;
        this.menuDTOMapper = menuDTOMapper;
    }

    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    @Transactional
    public Object createMenuFromRequest(MenuCreateRequest req) {
        Menu menu = new Menu();
        menu.setLastModified(LocalDateTime.now());
        menu.setLocationId(1); // Por ahora hardcodeado
        Set<MenuDay> days = new HashSet<>();
        
        for (MenuDayCreateRequest dayReq : req.getDays()) {
            MenuDay day = new MenuDay();
            day.setDay(MenuDay.DayOfWeek.valueOf(dayReq.getDay().toUpperCase()));
            day.setMenu(menu);
            day.setMeals(new HashSet<>());
            
            // Asegurar que solo haya un meal block por mealTime
            Map<MenuMeal.MealTime, MenuMealCreateRequest> mealMap = new HashMap<>();
            for (MenuMealCreateRequest mealReq : dayReq.getMeals()) {
                MenuMeal.MealTime mealTime = MenuMeal.MealTime.valueOf(mealReq.getMealTime().toUpperCase());
                if (mealMap.containsKey(mealTime)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Solo puede haber un bloque de comida por tipo (DESAYUNO, ALMUERZO, MERIENDA, CENA) en cada día");
                }
                mealMap.put(mealTime, mealReq);
            }
            
            // Crear los meal blocks únicos
            for (MenuMealCreateRequest mealReq : mealMap.values()) {
                MenuMeal meal = new MenuMeal();
                meal.setMealTime(MenuMeal.MealTime.valueOf(mealReq.getMealTime().toUpperCase()));
                meal.setMenuDay(day);
                
                List<Product> products = productRepository.findAllById(mealReq.getProductIds());
                meal.setProducts(products);
                
                day.getMeals().add(meal);
            }
            
            days.add(day);
        }
        
        menu.setDays(days);
        menu = menuRepository.save(menu);
        return menuDTOMapper.convertToDTO(menu);
    }

    @Transactional
    public DayMenuResponseDTO getMenuByDay(MenuDay.DayOfWeek day) {
        Menu menu = menuRepository.findByDays_Day(day)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menú no encontrado para " + day));
        
        MenuDay menuDay = menu.getDays().stream()
            .filter(d -> d.getDay().equals(day))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Día no encontrado en el menú"));
        
        return menuDTOMapper.convertDayToResponseDTO(menuDay);
    }

    @Transactional
    public DayMenuResponseDTO getCurrentMenu() {
        java.time.DayOfWeek today = LocalDateTime.now().getDayOfWeek();
        MenuDay.DayOfWeek menuDay = convertToMenuDay(today);

        if (menuDay == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay menú disponible los fines de semana");
        }

        return getMenuByDay(menuDay);
    }

    private MenuDay.DayOfWeek convertToMenuDay(java.time.DayOfWeek systemDay) {
        return switch (systemDay) {
            case MONDAY -> MenuDay.DayOfWeek.LUNES;
            case TUESDAY -> MenuDay.DayOfWeek.MARTES;
            case WEDNESDAY -> MenuDay.DayOfWeek.MIERCOLES;
            case THURSDAY -> MenuDay.DayOfWeek.JUEVES;
            case FRIDAY -> MenuDay.DayOfWeek.VIERNES;
            default -> null; // fin de semana
        };
    }

    @Transactional
    public MenuResponseDTO getMenu() {
        return menuRepository.findTopByOrderByLastModifiedDesc()
                .map(menuDTOMapper::convertToResponseDTO)
                .orElse(null);
    }

    public Menu updateMenu(Menu menu) {
        return menuRepository.save(menu);
    }

    @Transactional
    public MenuMealResponseDTO getMenuByDayAndMealTime(MenuDay.DayOfWeek day, MenuMeal.MealTime mealTime) {
        Menu menu = menuRepository.findByDays_Day(day)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menú no encontrado para " + day));
        
        MenuMeal meal = menu.getDays().stream()
            .filter(d -> d.getDay().equals(day))
            .flatMap(d -> d.getMeals().stream())
            .filter(m -> m.getMealTime().equals(mealTime))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comida no encontrada"));
        
        return menuDTOMapper.convertMealToResponseDTO(meal);
    }

    @Transactional
    public DayMenuResponseDTO updateDayMenu(MenuDay.DayOfWeek day, List<MenuMealCreateRequest> mealsRequest) {
        Menu menu = menuRepository.findByDays_Day(day)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menú no encontrado para " + day));
        
        // Encontrar el día específico
        MenuDay menuDay = menu.getDays().stream()
            .filter(d -> d.getDay().equals(day))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Día no encontrado"));
        
        // Validar que no haya duplicados de mealTime en la request
        Set<MenuMeal.MealTime> mealTimes = new HashSet<>();
        for (MenuMealCreateRequest mealReq : mealsRequest) {
            MenuMeal.MealTime mealTime = MenuMeal.MealTime.valueOf(mealReq.getMealTime().toUpperCase());
            if (!mealTimes.add(mealTime)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Solo puede haber un bloque de comida por tipo (DESAYUNO, ALMUERZO, MERIENDA, CENA)");
            }
        }
        
        // Actualizar solo los meal blocks especificados
        for (MenuMealCreateRequest mealReq : mealsRequest) {
            MenuMeal.MealTime mealTime = MenuMeal.MealTime.valueOf(mealReq.getMealTime().toUpperCase());
            
            // Buscar el meal block existente o crear uno nuevo
            MenuMeal meal = menuDay.getMeals().stream()
                .filter(m -> m.getMealTime().equals(mealTime))
                .findFirst()
                .orElse(null);
            
            if (meal == null) {
                // Crear nuevo meal block si no existe
                meal = new MenuMeal();
                meal.setMealTime(mealTime);
                meal.setMenuDay(menuDay);
                menuDay.getMeals().add(meal);
            }
            
            // Actualizar los productos
            List<Product> products = productRepository.findAllById(mealReq.getProductIds());
            meal.setProducts(products);
        }
        
        menu.setLastModified(LocalDateTime.now());
        menuRepository.save(menu);
        
        return menuDTOMapper.convertDayToResponseDTO(menuDay);
    }

    @Transactional
    public MenuMealResponseDTO updateMealMenu(MenuDay.DayOfWeek day, MenuMeal.MealTime mealTime, List<Long> productIds) {
        Menu menu = menuRepository.findByDays_Day(day)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menú no encontrado para " + day));
        
        // Encontrar el día específico
        MenuDay menuDay = menu.getDays().stream()
            .filter(d -> d.getDay().equals(day))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Día no encontrado"));
        
        // Buscar el meal block existente o crear uno nuevo
        MenuMeal meal = menuDay.getMeals().stream()
            .filter(m -> m.getMealTime().equals(mealTime))
            .findFirst()
            .orElse(null);
        
        if (meal == null) {
            // Crear nuevo meal block si no existe
            meal = new MenuMeal();
            meal.setMealTime(mealTime);
            meal.setMenuDay(menuDay);
            menuDay.getMeals().add(meal);
        }
        
        // Actualizar los productos
        List<Product> products = productRepository.findAllById(productIds);
        meal.setProducts(products);
        
        menu.setLastModified(LocalDateTime.now());
        menuRepository.save(menu);
        
        return menuDTOMapper.convertMealToResponseDTO(meal);
    }

}