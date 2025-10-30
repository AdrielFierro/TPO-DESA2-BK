package com.uade.comedor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "menus")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuid;

    @Column(nullable = false)
    private LocalDateTime lastModified;

    @Column(nullable = false)
    private int location_id;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MenuDay> days;

    // Getters and Setters
    public Long getId() {
        return menuid;
    }


    public List<MenuDay> getDays() {
        return days;
    }

    public void setDays(List<MenuDay> days) {
        this.days = days;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public int getLocationId() {
        return location_id;
    }

    public void setLocationId(int locationId) {
        this.location_id = locationId;
    }


}