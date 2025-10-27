package com.uade.comedor.dto;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.List;

public class WeeklyMenuDTO {
    private Map<String, List<MealBlockDTO>> dailyMenus;
    private ZonedDateTime lastModified;

    public Map<String, List<MealBlockDTO>> getDailyMenus() {
        return dailyMenus;
    }

    public void setDailyMenus(Map<String, List<MealBlockDTO>> dailyMenus) {
        this.dailyMenus = dailyMenus;
    }

    public ZonedDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(ZonedDateTime lastModified) {
        this.lastModified = lastModified;
    }
}