package com.uade.comedor.dto;

import java.time.ZonedDateTime;
import java.util.List;

public class MenuDTO {
    private ZonedDateTime lastModified;
    private List<MenuDayDTO> days;

    // Getters and Setters
    public ZonedDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(ZonedDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public List<MenuDayDTO> getDays() {
        return days;
    }

    public void setDays(List<MenuDayDTO> days) {
        this.days = days;
    }
}