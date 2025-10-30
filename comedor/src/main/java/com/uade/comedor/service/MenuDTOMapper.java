package com.uade.comedor.service;

import com.uade.comedor.dto.*;
import com.uade.comedor.entity.*;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuDTOMapper {
    
    public MenuResponseDTO convertToResponseDTO(Menu menu) {
        if (menu == null) {
            return null;
        }
        
        MenuResponseDTO dto = new MenuResponseDTO();
        dto.setLastModified(ZonedDateTime.of(menu.getLastModified(), ZoneId.systemDefault()));
        dto.setDays(convertDaysToResponseDTO(menu.getDays()));
        
        return dto;
    }
    
    public MenuDTO convertToDTO(Menu menu) {
        if (menu == null) {
            return null;
        }
        
        MenuDTO dto = new MenuDTO();
        dto.setLastModified(ZonedDateTime.of(menu.getLastModified(), ZoneId.systemDefault()));
        dto.setDays(convertDaysToDTO(menu.getDays()));
        
        return dto;
    }
    
    private List<MenuDayResponseDTO> convertDaysToResponseDTO(Set<MenuDay> days) {
        if (days == null) {
            return new ArrayList<>();
        }
        
        return days.stream().map(day -> {
            MenuDayResponseDTO dto = new MenuDayResponseDTO();
            dto.setDay(day.getDay().toString().toLowerCase());
            dto.setMeals(convertMealsToResponseDTO(day.getMeals()));
            return dto;
        }).collect(Collectors.toList());
    }
    
    private List<MenuMealResponseDTO> convertMealsToResponseDTO(Set<MenuMeal> meals) {
        if (meals == null) {
            return new ArrayList<>();
        }
        
        return meals.stream().map(meal -> {
            MenuMealResponseDTO dto = new MenuMealResponseDTO();
            dto.setMealTime(meal.getMealTime().toString().toLowerCase());
            dto.setSections(organizeProductsBySections(meal.getProducts()));
            return dto;
        }).collect(Collectors.toList());
    }
    
    private Map<String, List<ProductDTO>> organizeProductsBySections(List<Product> products) {
        Map<String, List<ProductDTO>> sections = new HashMap<>();
        sections.put("platos", new ArrayList<>());
        sections.put("bebidas", new ArrayList<>());
        sections.put("postres", new ArrayList<>());
        
        if (products != null) {
            products.forEach(product -> {
                ProductDTO productDTO = convertProductToDTO(product);
                String section = mapProductTypeToSection(product.getProductType());
                sections.get(section).add(productDTO);
            });
        }
        
        return sections;
    }
    
    private String mapProductTypeToSection(Product.ProductType productType) {
        return switch (productType) {
            case BEBIDA -> "bebidas";
            case POSTRE -> "postres";
            case PLATO -> "platos";
        };
    }
    
    public DayMenuResponseDTO convertDayToResponseDTO(MenuDay day) {
        if (day == null) {
            return null;
        }
        
        DayMenuResponseDTO dto = new DayMenuResponseDTO();
        dto.setDay(day.getDay().toString().toLowerCase());
        dto.setMeals(convertMealsToResponseDTO(day.getMeals()));
        return dto;
    }
    
    public MenuMealResponseDTO convertMealToResponseDTO(MenuMeal meal) {
        if (meal == null) {
            return null;
        }
        
        MenuMealResponseDTO dto = new MenuMealResponseDTO();
        dto.setMealTime(meal.getMealTime().toString().toLowerCase());
        dto.setSections(organizeProductsBySections(meal.getProducts()));
        return dto;
    }
    
    private List<MenuDayDTO> convertDaysToDTO(Set<MenuDay> days) {
        if (days == null) {
            return new ArrayList<>();
        }
        
        return days.stream().map(day -> {
            MenuDayDTO dto = new MenuDayDTO();
            dto.setDay(day.getDay());
            dto.setMeals(convertMealsToDTO(day.getMeals()));
            return dto;
        }).collect(Collectors.toList());
    }
    
    private List<MenuMealDTO> convertMealsToDTO(Set<MenuMeal> meals) {
        if (meals == null) {
            return new ArrayList<>();
        }
        
        return meals.stream().map(meal -> {
            MenuMealDTO dto = new MenuMealDTO();
            dto.setMealTime(meal.getMealTime());
            dto.setProductIds(meal.getProducts().stream()
                .map(Product::getId)
                .collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }
    
    private ProductDTO convertProductToDTO(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setProductType(product.getProductType().toString());
        dto.setImageUrl(product.getImageUrl() != null ? product.getImageUrl() : "");
        return dto;
    }
    

}