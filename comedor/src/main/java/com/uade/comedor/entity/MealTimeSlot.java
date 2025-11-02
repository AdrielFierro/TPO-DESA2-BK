package com.uade.comedor.entity;

import java.time.LocalTime;

public enum MealTimeSlot {
    DESAYUNO_SLOT_1("DESAYUNO", LocalTime.of(7, 0), LocalTime.of(8, 0)),
    DESAYUNO_SLOT_2("DESAYUNO", LocalTime.of(8, 0), LocalTime.of(9, 0)),
    
    ALMUERZO_SLOT_1("ALMUERZO", LocalTime.of(12, 0), LocalTime.of(13, 0)),
    ALMUERZO_SLOT_2("ALMUERZO", LocalTime.of(13, 0), LocalTime.of(14, 0)),
    
    MERIENDA_SLOT_1("MERIENDA", LocalTime.of(16, 0), LocalTime.of(17, 0)),
    MERIENDA_SLOT_2("MERIENDA", LocalTime.of(17, 0), LocalTime.of(18, 0)),
    
    CENA_SLOT_1("CENA", LocalTime.of(20, 0), LocalTime.of(21, 0)),
    CENA_SLOT_2("CENA", LocalTime.of(21, 0), LocalTime.of(22, 0));

    private final String mealType;
    private final LocalTime startTime;
    private final LocalTime endTime;

    MealTimeSlot(String mealType, LocalTime startTime, LocalTime endTime) {
        this.mealType = mealType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getMealType() {
        return mealType;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public static MealTimeSlot[] getSlotsByMealType(String mealType) {
        return java.util.Arrays.stream(values())
                .filter(slot -> slot.getMealType().equalsIgnoreCase(mealType))
                .toArray(MealTimeSlot[]::new);
    }
}
