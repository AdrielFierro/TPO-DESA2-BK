package com.uade.comedor.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.uade.comedor.entity.Reservation;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStartDateTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<Reservation> findByStartDateTimeAfter(LocalDateTime start, Pageable pageable);
    List<Reservation> findByStartDateTimeBefore(LocalDateTime end, Pageable pageable);
    List<Reservation> findByUserId(Long userId);
}