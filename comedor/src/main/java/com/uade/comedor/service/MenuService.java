package com.uade.comedor.service;

import com.uade.comedor.entity.*;
import com.uade.comedor.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Arrays;
 

@Service
public class MenuService {
    private final MenuRepository menuRepository;
    
    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    public Menu getCurrentMenu() {
        // Obtener el día actual
        DayOfWeek currentDay = LocalDateTime.now().getDayOfWeek();
        Menu.DayOfWeek menuDay = convertToMenuDay(currentDay);
        
        // Si es fin de semana, lanzar excepción
        if (menuDay == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay menú disponible los fines de semana");
        }

        // Since there's a single Menu in the system, fetch it and verify the day
        return menuRepository.findAll().stream()
            .findFirst()
            .filter(m -> m.getDay() == menuDay)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay menú configurado para hoy"));
    }

    public List<MealShift> getMealShifts() {
        return Arrays.asList(
            createMealShift(MealTime.DESAYUNO, "07:00-12:00"),
            createMealShift(MealTime.ALMUERZO, "12:00-16:00"),
            createMealShift(MealTime.MERIENDA, "16:00-20:00"),
            createMealShift(MealTime.CENA, "20:00-22:00")
        );
    }

    public Menu getMenuByDay(Menu.DayOfWeek day) {
        return menuRepository.findAll().stream()
            .findFirst()
            .filter(m -> m.getDay() == day)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menú no encontrado para el día " + day));
    }

    @Transactional
    public Menu createMenu(Menu menu) {
        // En este sistema solo debe existir un Menu. Rechazar creación si ya existe uno.
        if (menuRepository.count() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un menú en el sistema");
        }
        // Asegurar que las referencias padre-hijo estén seteadas para JPA
        if (menu.getMeals() != null) {
            menu.getMeals().forEach(mealBlock -> {
                mealBlock.setMenu(menu);
                if (mealBlock.getSections() != null) {
                    mealBlock.getSections().forEach(section -> section.setMealBlock(mealBlock));
                }
            });
        }

        menu.setLastModified(LocalDateTime.now());
        return menuRepository.save(menu);
    }

    @Transactional
    public Menu updateMenu(Menu menu) {
        // Usar el único menú existente
        Menu existingMenu = menuRepository.findAll().stream()
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay menú creado aún"));

        // Mantener el ID de la entidad existente
        menu.setId(existingMenu.getId());

        // Asegurar que las referencias padre-hijo estén seteadas para JPA
        if (menu.getMeals() != null) {
            menu.getMeals().forEach(mealBlock -> {
                mealBlock.setMenu(menu);
                if (mealBlock.getSections() != null) {
                    mealBlock.getSections().forEach(section -> section.setMealBlock(mealBlock));
                }
            });
        }

        menu.setLastModified(LocalDateTime.now());
        return menuRepository.save(menu);
    }

    public MealBlock getMenuByDayAndMealTime(Menu.DayOfWeek day, MealTime mealTime) {
        Menu menu = getMenuByDay(day);
        return menu.getMeals().stream()
            .filter(meal -> meal.getMealTime() == mealTime)
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "No se encontró el turno " + mealTime + " para el día " + day));
    }

    private MealShift createMealShift(MealTime mealTime, String schedule) {
        MealShift shift = new MealShift();
        shift.setMealTime(mealTime);
        shift.setSchedule(schedule);
        return shift;
    }

    private Menu.DayOfWeek convertToMenuDay(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> Menu.DayOfWeek.LUNES;
            case TUESDAY -> Menu.DayOfWeek.MARTES;
            case WEDNESDAY -> Menu.DayOfWeek.MIERCOLES;
            case THURSDAY -> Menu.DayOfWeek.JUEVES;
            case FRIDAY -> Menu.DayOfWeek.VIERNES;
            default -> null;
        };
    }
}