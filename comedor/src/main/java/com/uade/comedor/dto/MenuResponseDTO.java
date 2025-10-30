package com.uade.comedor.dto;

import java.time.ZonedDateTime;
import java.util.List;

public class MenuResponseDTO {
    private ZonedDateTime lastModified;
    private List<MenuDayResponseDTO> days;

    // Getters y Setters
    public ZonedDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(ZonedDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public List<MenuDayResponseDTO> getDays() {
        return days;
    }

    public void setDays(List<MenuDayResponseDTO> days) {
        this.days = days;
    }
}