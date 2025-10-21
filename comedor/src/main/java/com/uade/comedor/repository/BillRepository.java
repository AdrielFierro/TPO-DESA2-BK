package com.uade.comedor.repository;

import com.uade.comedor.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}