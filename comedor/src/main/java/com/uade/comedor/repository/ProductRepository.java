package com.uade.comedor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.uade.comedor.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Métodos personalizados opcionales
}