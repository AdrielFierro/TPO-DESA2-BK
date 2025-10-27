package com.uade.comedor.dto;

import java.util.List;

public class MenuCreateRequest {
    private List<MenuDayCreateRequest> days;

    public List<MenuDayCreateRequest> getDays() {
        return days;
    }

    public void setDays(List<MenuDayCreateRequest> days) {
        this.days = days;
    }
}
